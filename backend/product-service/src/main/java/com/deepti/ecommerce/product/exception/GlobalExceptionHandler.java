package com.deepti.ecommerce.product.exception;


import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleRuntimeException(RuntimeException ex)
    {
        return Map.of("timestamp", LocalDateTime.now(),
                      "message", ex.getMessage(),
                    "status", HttpStatus.NOT_FOUND.value());
    }

}
