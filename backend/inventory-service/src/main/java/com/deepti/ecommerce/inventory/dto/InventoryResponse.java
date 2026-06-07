package com.deepti.ecommerce.inventory.dto;

public record InventoryResponse(
        Long id,
        Long productId,
        Integer availableQuantity,
        Integer reservedQuantity ) {
}
