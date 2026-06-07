package com.deepti.ecommerce.inventory.exception;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

@ExceptionHandler(InventoryNotFoundException.class)
@ResponseStatus(HttpStatus.NOT_FOUND)
public  Map<String, Object> handleInventoryNotFound(InventoryNotFoundException ex)
{
    return Map.of("timestamp", LocalDateTime.now(),
"status", HttpStatus.NOT_FOUND.value(),
"message",ex.getMessage());
}

@ExceptionHandler(InsufficientInventoryException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public Map<String,Object> handleInsufficientInventory(InsufficientInventoryException ex)
{
    return Map.of( "timestamp", LocalDateTime.now(),
                    "status", HttpStatus.BAD_REQUEST.value(),
                    "message", ex.getMessage());

}

@ExceptionHandler(Exception.class)
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public Map<String,Object> handleGenericException( Exception ex)
{
    return Map.of(
        "timestamp", LocalDateTime.now(),
        "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "message", ex.getMessage()
    );
}


}
