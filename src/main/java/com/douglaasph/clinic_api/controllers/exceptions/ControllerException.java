package com.douglaasph.clinic_api.controllers.exceptions;

import com.douglaasph.clinic_api.services.exceptions.DatabaseException;
import com.douglaasph.clinic_api.services.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ControllerException extends RuntimeException {

    // ERROR 404 (Not found)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> resourceNotFound(ResourceNotFoundException e, HttpServletRequest request) {
        String error = "Resource not found";
        HttpStatus status = HttpStatus.NOT_FOUND;
        StandardError err = new StandardError(Instant.now(), e.getMessage(), request.getRequestURI(), error, status.value());
        return ResponseEntity.status(status).body(err);
    }

    // ERROR 400 (Database error or syntax error)
    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<StandardError> databaseError(DatabaseException e, HttpServletRequest request) {
        String error = "Database error";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(Instant.now(), e.getMessage(), request.getRequestURI(), error, status.value());
        return ResponseEntity.status(status).body(err);
    }

    // ERROR 500 (Internal server error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> genericError(Exception e, HttpServletRequest request) {
        String error = "Internal server error";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        StandardError err = new StandardError(Instant.now(), e.getMessage(), request.getRequestURI(), error, status.value());
        return ResponseEntity.status(status).body(err);
    }

    // ERROR 401
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<StandardError> badCredentials(BadCredentialsException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED; // Status 401
        StandardError err = new StandardError();
        err.setTimestamp(Instant.now());
        err.setStatus(status.value());
        err.setError("Unauthorized");
        err.setMessage("Invalid email or password.");
        err.setPath(request.getRequestURI());

        return ResponseEntity.status(status).body(err);
    }

    // ERROR 400 (Token invalid or expired)
    @ExceptionHandler(TokenException.class)
    public ResponseEntity<StandardError> tokenException(TokenException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST; // Status 400
        StandardError err = new StandardError();
        err.setTimestamp(Instant.now());
        err.setStatus(status.value());
        err.setError("Token error");
        err.setMessage(e.getMessage());
        err.setPath(request.getRequestURI());

        return ResponseEntity.status(status).body(err);
    }
}
