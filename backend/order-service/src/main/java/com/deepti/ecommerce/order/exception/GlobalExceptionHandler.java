package com.deepti.ecommerce.order.exception;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

@ExceptionHandler(RuntimeException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public Map<String,Object> handleRuntimeException(RuntimeException ex)
{
    return Map.of("timestamp", LocalDateTime.now(),
                     "status", HttpStatus.BAD_REQUEST.value(),
                     "message", ex.getMessage() != null ? ex.getMessage() : "Unexpected error"
);
        
    
}

 @ExceptionHandler(PaymentRequestConflictException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentConflict(
            PaymentRequestConflictException ex,
            HttpServletRequest request) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", Instant.now());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", "Conflict");
        response.put("message", ex.getMessage());
        response.put("path", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }



}
