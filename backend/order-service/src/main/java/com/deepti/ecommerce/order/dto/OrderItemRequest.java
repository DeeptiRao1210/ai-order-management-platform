package com.deepti.ecommerce.order.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
    @NotNull Long productId,  
    @NotNull  @Min(1) Integer quantity,
    @NotNull @DecimalMin("0.1") BigDecimal price

) {

}
