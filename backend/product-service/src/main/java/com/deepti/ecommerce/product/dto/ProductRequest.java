package com.deepti.ecommerce.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductRequest( 

        @NotBlank(message = "Product Name is required")
        String name,
        String description,
        @NotNull(message = "Price is Required")
        @DecimalMin(value = "0.1", message = "Price must be greater than 0")
        BigDecimal price,
        @NotBlank(message = "Category is required")
        String category

){


}
