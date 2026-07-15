package com.deepti.ecommerce.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryRequest(
     @NotNull(message = "The product ID is required.")
    Long productId,


    @NotNull(message = "Available quantity is required.")
    @Min(value = 0, message = "Quantity cannot be negative.")
    Integer availableQuantity
) {

}




