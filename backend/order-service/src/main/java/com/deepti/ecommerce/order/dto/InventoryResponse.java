package com.deepti.ecommerce.order.dto;

public record InventoryResponse(
        Long id,
        Long productId,
        Integer availableQuantity,
        Integer reservedQuantity ) {
}
