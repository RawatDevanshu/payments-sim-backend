package com.devh.payment_sim.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.devh.payment_sim.core.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // DB data integrity voilation errors
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleDataIntegrityVoilation(DataIntegrityViolationException ex){
        log.error("Data integrity violation occurred: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("The request violates a data constraint. Please adjust and retry."));
    }
    
    // All Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
        
        log.warn("Validation failed with {} field errors", errors.size());
        ApiResponse<Map<String, String>> response = ApiResponse.error("Validation Failed!", errors);
                    
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleNotFound(ResourceNotFoundException ex){
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<String>> handleConflict(ConflictException ex){
        log.warn("Conflict exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InvalidPinException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidPin(InvalidPinException ex) {
        log.warn("Invalid PIN attempt: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ApiResponse<String>> handleInsufficient(InsufficientFundsException ex) {
        log.warn("Insufficient funds: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleBadRequest(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(PinNotSetException.class)
    public ResponseEntity<ApiResponse<String>> handlePinNotSet(PinNotSetException ex){
        log.warn("PIN not set: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<String>> handleAuthException(AuthenticationException ex) {
        log.warn("Authentication exception: {}", ex.getMessage());
        log.debug("Authentication exception details: ", ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ex.getMessage()));
    }

    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Invalid JSON format received: {}", ex.getMessage());
        String hint = "Invalid JSON format";
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(hint));
    }


    // fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericExceptions(Exception ex) {
        log.error("Unexpected exception caught: {}", ex.getMessage(), ex);
        ApiResponse<String> response = ApiResponse.error("Internal Server Error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
}
