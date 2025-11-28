package com.devh.payment_sim.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.devh.payment_sim.core.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // All Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
        
        ApiResponse<Map<String, String>> response = ApiResponse.
                    <Map<String,String>>builder()
                    .status("error")
                    .message("Validation failed")
                    .data(errors)
                    .timestamp(Instant.now())
                    .build();
                    
        return ResponseEntity.badRequest().body(response);
    }

    // generic exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericExceptions(Exception ex) {
        ApiResponse<String> response = ApiResponse.<String>builder()
            .status("error")
            .message("Internal Server error")
            .data(ex.getMessage())
            .timestamp(Instant.now())
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
}
