package com.deepti.ecommerce.inventory.exception;

public class InventoryNotFoundException extends RuntimeException {

    public InventoryNotFoundException(String message)
    {
        super(message);
    }

}
