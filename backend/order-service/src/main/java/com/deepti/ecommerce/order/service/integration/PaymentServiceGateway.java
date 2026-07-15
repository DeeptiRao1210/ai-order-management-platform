package com.deepti.ecommerce.order.service.integration;

import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

import com.deepti.ecommerce.order.client.PaymentClient;
import com.deepti.ecommerce.order.dto.PaymentRequest;
import com.deepti.ecommerce.order.dto.PaymentResponse;

@Service
public class PaymentServiceGateway {

    private final PaymentClient paymentClient;
    private final CircuitBreakerFactory<?,?> circuitBreakerFactory;
    
    public PaymentServiceGateway(PaymentClient paymentClient, CircuitBreakerFactory<?,?> circuitBreakerFactory)
    {
        this.paymentClient = paymentClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public PaymentResponse processPayment(PaymentRequest request)
    {
        return circuitBreakerFactory.create("paymentServiceCircuitBreaker")
        .run(
            ()->paymentClient.processPayment(request),
            throwable-> paymentFallback(request, throwable)

        );
    }

    private PaymentResponse paymentFallback(PaymentRequest request, Throwable throwable)
    {
       
        System.err.println(
                "Payment service failed for orderId="
                        + request.orderId()
                        + ", reason="
                        + throwable.getMessage()
        );

       
        return new PaymentResponse(null,
                                    request.orderId(),
                                    request.amount(),
                                    null,
                                    "PENDING"
        );
    }

}
