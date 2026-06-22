package com.deepti.ecommerce.order.entity;

public enum OrderStatus {
    PENDING,
    INVENTORY_RESERVED,
    PAYMENT_COMPLETED,
    CONFIRMED,
    CANCELLED,
    FAILED
}
