package com.deepti.ecommerce.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryResponse(
        Long id,
        Long productId,
        Integer availableQuantity,
       Integer reservedQuantity  ) {

    
}
