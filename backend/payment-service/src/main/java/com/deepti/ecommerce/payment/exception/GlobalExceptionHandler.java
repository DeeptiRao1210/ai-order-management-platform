package com.deepti.ecommerce.payment.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IdempotencyKeyConflictException.class)
    public ResponseEntity<Map<String,Object>> handleIdempotencyKeyConflict
        (IdempotencyKeyConflictException exception, HttpServletRequest request)
    {

            return buildResponse(org.springframework.http.HttpStatus.CONFLICT,
                exception.getMessage(),request.getRequestURI());
            

    }                                            

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String,Object>> handlingMissingRequestHeader(MissingRequestHeaderException exception, HttpServletRequest request)
    {
        String message = exception.getHeaderName() + " header is required.";
        return buildResponse(org.springframework.http.HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request)
    {
         return buildResponse(org.springframework.http.HttpStatus.BAD_REQUEST,
                exception.getMessage(),request.getRequestURI());

    }

    private ResponseEntity<Map<String, Object>> buildResponse( org.springframework.http.HttpStatus status, String message, String path)
    {
        Map<String,Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        return ResponseEntity.status(status).body(body);
        
    }

}
