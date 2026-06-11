package com.deepti.ecommerce.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.deepti.ecommerce.order.entity.OrderStatus;

public record OrderResponse(
Long orderId,
Long userId,
BigDecimal totalAmount,
OrderStatus status,
LocalDateTime createdAt

) {

}
