package com.deepti.ecommerce.inventory.exception;

public class InsufficientInventoryException extends RuntimeException{


    public InsufficientInventoryException(String message) {
        super(message);
    }
}
