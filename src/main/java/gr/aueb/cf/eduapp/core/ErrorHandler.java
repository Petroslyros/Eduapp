package gr.aueb.cf.eduapp.core;

import gr.aueb.cf.eduapp.core.exceptions.*;
import gr.aueb.cf.eduapp.core.exceptions.AppServerException;
import gr.aueb.cf.eduapp.dto.ResponseMessageDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {

        log.error("validation failed. Message{}", e.getMessage(), e);
        BindingResult bindingResult = e.getBindingResult();

        Map<String,String> errors = new HashMap<>();
        for(FieldError fieldError: bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AppObjectNotFoundException.class)
    public ResponseEntity<ResponseMessageDTO> handleConstraintViolationException(AppObjectNotFoundException e) {
        log.warn("Not found. Message={}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)                                    // HttpCode
                .body(new ResponseMessageDTO(e.getCode(), e.getMessage()));
    }
    @ExceptionHandler(AppObjectInvalidArgumentException.class)
    public ResponseEntity<ResponseMessageDTO> handleConstraintViolationException(AppObjectInvalidArgumentException e) {
        log.warn("Invalid Argument. Message={}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ResponseMessageDTO(e.getCode(), e.getMessage()));
    }
    @ExceptionHandler(AppObjectAlreadyExists.class)
    public ResponseEntity<ResponseMessageDTO> handleConstraintViolationException(AppObjectAlreadyExists e) {
        log.warn("Already exists. Message={}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)                                     // Http 409 already exists
                .body(new ResponseMessageDTO(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AppObjectNotAuthorizedException.class)
    public ResponseEntity<ResponseMessageDTO> handleConstraintViolationException(AppObjectNotAuthorizedException e, WebRequest request) {
        log.warn("Authorization failed for URI={}. Message={}",request.getDescription(false), e.getMessage()); // uri/ap/user/....
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)                                     // Http 403
                .body(new ResponseMessageDTO(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AppServerException.class)
    public ResponseEntity<ResponseMessageDTO> handleConstraintViolationException(AppServerException e) {
        log.warn("Server error. Message={}", e.getMessage()); // uri/ap/user/....
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseMessageDTO(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ResponseMessageDTO> handleConstraintViolationException(IOException e) {
        log.error("File upload failed. Message={}", e.getMessage()); // uri/ap/user/....
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseMessageDTO("FILE_UPLOAD_FAILED", e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseMessageDTO> handleBadCredentials(BadCredentialsException e, HttpServletRequest request) {
        log.warn("Failed login attempt for IP{}", request.getRemoteAddr());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)                                     // Http 401
                .body(new ResponseMessageDTO("UNAUTHORIZED", e.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseMessageDTO> handleBadCredentials(AuthenticationException e, HttpServletRequest request) {
        log.warn("Failed login for IP{}", request.getRemoteAddr());

        String errorCode = switch (e.getClass().getSimpleName()) {
            case "DisabledException" -> "ACCOUNT_DISABLED";
            case "LockedException" -> "ACCOUNT_LOCKED";
            case "AccountExpiredException" -> "ACCOUNT_EXPIRED";
            case "CredentialsExpiredException" -> "CREDENTIALS_EXPIRED";
            default -> "ACCOUNT_ERROR";
        };

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)                                     // Http 403
                .body(new ResponseMessageDTO(errorCode, e.getMessage()));
    }

}
