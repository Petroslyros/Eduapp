package gr.aueb.cf.eduapp.security;

import gr.aueb.cf.eduapp.authentication.CustomUserDetailsService;
import gr.aueb.cf.eduapp.authentication.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * A filter that runs once per request (OncePerRequestFilter) to handle
 * JWT-based authentication.
 * It checks the Authorization header, validates the JWT,
 * and sets the authenticated user in the SecurityContext if valid.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;                 // Utility for extracting/validating JWTs
    private final UserDetailsService userDetailsService; // Loads user details from DB (via username)

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Extract the "Authorization" header (should be: "Bearer <token>")
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // If no header or it doesn't start with "Bearer", skip JWT check
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // continue without authentication
            return;
        }

        // Remove "Bearer " prefix and get the JWT string
        jwt = authHeader.substring(7).trim();

        try {
            // Extract the "subject" (username) from the token
            username = jwtService.extractSubject(jwt);

            // Only authenticate if we got a username and no auth exists yet in SecurityContext
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Load the user details from DB
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Validate the token against the user details (e.g., expiration, signature, etc.)
                if (!jwtService.isTokenValid(jwt, userDetails)) {
                    throw new BadCredentialsException("Invalid Token");
                }

                // Build an Authentication object (with roles/authorities from UserDetails)
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, // principal (the authenticated user)
                                null,        // credentials (not needed here)
                                userDetails.getAuthorities() // roles/permissions
                        );

                // Store authentication info in the SecurityContext → makes user "logged in"
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (ExpiredJwtException e) {
            // Token is expired → triggers AuthenticationEntryPoint → results in 401 Unauthorized
            throw new AuthenticationCredentialsNotFoundException("Expired token", e);
        } catch (JwtException | IllegalArgumentException e) {
            // Token is invalid → triggers AuthenticationEntryPoint → results in 401 Unauthorized
            throw new BadCredentialsException("Invalid token");
        } catch (Exception e) {
            // Something else failed (unexpected) → triggers AccessDeniedHandler → 403 Forbidden
            throw new AccessDeniedException("Token validation failed");
        }

        // Continue request processing (now with authentication set, if token was valid)
        filterChain.doFilter(request, response);
    }
}
