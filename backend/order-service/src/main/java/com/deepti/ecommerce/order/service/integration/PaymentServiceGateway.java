package com.deepti.ecommerce.order.service.integration;

import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

import com.deepti.ecommerce.order.client.PaymentClient;
import com.deepti.ecommerce.order.dto.PaymentRequest;
import com.deepti.ecommerce.order.dto.PaymentResponse;
import com.deepti.ecommerce.order.exception.PaymentRequestConflictException;

import feign.FeignException;

@Service
public class PaymentServiceGateway {

    private final PaymentClient paymentClient;
    private final CircuitBreakerFactory<?,?> circuitBreakerFactory;
    
    public PaymentServiceGateway(PaymentClient paymentClient, CircuitBreakerFactory<?,?> circuitBreakerFactory)
    {
        this.paymentClient = paymentClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public PaymentResponse processPayment(String idempotencyKey, PaymentRequest request)
    {
        return circuitBreakerFactory.create("paymentServiceCircuitBreaker")
        .run(
            ()->callPaymentService(idempotencyKey,request),
            throwable-> paymentFallback(request, throwable)

        );
    }

    private PaymentResponse callPaymentService(String idempotencyKey, PaymentRequest request)
    {
        try{

            return paymentClient.processPayment(idempotencyKey, request);

        }
        catch(FeignException.BadRequest exception)
        {
                throw new IllegalArgumentException( extractFeignMessage(exception), exception);
        }
        catch(FeignException.Conflict exception)
        {
                throw new PaymentRequestConflictException( extractFeignMessage(exception), exception);
        }


    }

    private  Throwable findActualCause(Throwable throwable)
    {
        Throwable current = throwable;
        while(current.getCause()!= null && current.getCause() != current)
        {
            current = current.getCause();
        }
        return current;
    }


    private String extractFeignMessage(FeignException exception)
    {
        String body = exception.contentUTF8();
        if(body == null || body.isBlank())
        {
            return exception.getMessage();
        }
        return body;
    }



    private PaymentResponse paymentFallback(PaymentRequest request, Throwable throwable)
    {
       
        Throwable actualCause = findActualCause(throwable);

        if (actualCause instanceof IllegalArgumentException
                illegalArgumentException) {

            throw illegalArgumentException;
        }

        if (actualCause instanceof PaymentRequestConflictException
                conflictException) {

            throw conflictException;
        }
        
       /*  System.err.println(
                "Payment service failed for orderId="
                        + request.orderId()
                        + ", reason="
                        + throwable.getMessage()
        ); */

       
        return new PaymentResponse(null,
                                    request.orderId(),
                                    request.amount(),
                                    null,
                                    "PENDING"
        );
    }

}
