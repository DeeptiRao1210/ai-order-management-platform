package com.deepti.ecommerce.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(

        Long id,
        String name,
        String description,
        BigDecimal price,
        String category,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
