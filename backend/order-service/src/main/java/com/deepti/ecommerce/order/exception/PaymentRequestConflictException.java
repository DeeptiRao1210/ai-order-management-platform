package com.deepti.ecommerce.order.exception;

public class PaymentRequestConflictException extends RuntimeException{

    public PaymentRequestConflictException(String message) {
        super(message);
    }

    public PaymentRequestConflictException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}
