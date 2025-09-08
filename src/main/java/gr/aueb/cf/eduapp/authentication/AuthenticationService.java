package gr.aueb.cf.eduapp.authentication;

import gr.aueb.cf.eduapp.dto.AuthenticationRequestDTO;
import gr.aueb.cf.eduapp.dto.AuthenticationResponseDTO;
import gr.aueb.cf.eduapp.model.User;
import gr.aueb.cf.eduapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service // Marks this class as a Spring service (a singleton bean for business logic)
@RequiredArgsConstructor // Lombok annotation: creates a constructor with all final fields
public class AuthenticationService {

    private final JwtService jwtService; // Generates and validates JWT tokens
    private final UserRepository userRepository; // Used to look up users (though not directly used here)
    private final AuthenticationManager authenticationManager; // Authenticates credentials (username + password)

    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO dto) {
        // This performs the actual authentication step.
        // If the credentials are wrong, an exception is thrown here.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password())
        );

        // Gets the authenticated user (Spring Security stores it as the principal)
        User user = (User) authentication.getPrincipal();

        // Generates a JWT for the authenticated user, including the user's role
        String token = jwtService.generateToken(authentication.getName(), user.getRole().name());

        // Return user's info and token in a DTO to the frontend
        return new AuthenticationResponseDTO(user.getFirstname(), user.getLastname(), token);
    }
}

