package com.deepti.ecommerce.payment.service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.deepti.ecommerce.payment.dto.PaymentRequest;
import com.deepti.ecommerce.payment.dto.PaymentResponse;
import com.deepti.ecommerce.payment.entity.Payment;
import com.deepti.ecommerce.payment.entity.PaymentStatus;
import com.deepti.ecommerce.payment.exception.IdempotencyKeyConflictException;
import com.deepti.ecommerce.payment.repository.PaymentRepository;



@Service
public class PaymentService {

    
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository)
    {
        this.paymentRepository = paymentRepository;
        //this.paymentRequest = paymentRequest;
    }


    public PaymentResponse processPayment(String idempotencyKey, PaymentRequest request)
    {
       validateInput(idempotencyKey, request);
       Payment existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey).orElse(null);

       if(existingPayment != null)
       {
            validateSameRequest(existingPayment,request);
            return toResponse(existingPayment);

       }
       
        Payment payment = new Payment();
        payment.setIdempotencyKey(idempotencyKey);
        payment.setOrderId(request.orderId());
        payment.setAmount(request.amount());
        payment.setPaymentMethod("CARD");
        payment.setStatus(PaymentStatus.PROCESSING);

        try{

            payment = paymentRepository.saveAndFlush(payment);
        }
       catch(DataIntegrityViolationException ex)
       {
            Payment concurrentPayment =  paymentRepository.findByIdempotencyKey(idempotencyKey)
                                         .orElseThrow(()-> new IllegalStateException("Payment record could not be retrieved"+
                                        " after idempotency-key conflict.", ex));

            validateSameRequest(concurrentPayment, request);
            return toResponse(concurrentPayment);
       }
       
       return simulatePayment(payment);
       
      

    }

    private PaymentResponse simulatePayment(Payment payment)
    {
        try{
            boolean successful = ThreadLocalRandom.current().nextInt(100) < 80;
            if(successful)
            {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setTransactionId("TXN"+ UUID.randomUUID());
                payment.setFailureReason(null);        
            }
            else
            {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setTransactionId(null);
                payment.setFailureReason(
                        "Payment declined by simulated payment processor"
                );
            }
        }
        catch(Exception ex)
        {
            payment.setStatus(PaymentStatus.UNKNOWN);
            payment.setTransactionId(null);
            payment.setFailureReason("Payment outcome requires verification");
        }

        Payment savedPayment = paymentRepository.save(payment);
        return toResponse(savedPayment);
    }




    public PaymentResponse getPaymentByOrderId(Long orderId)
    {
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(()-> 
                          new RuntimeException("Payment not found for Order Id:"+ orderId)
                         );

        return toResponse(payment);

    }
    
     private void validateSameRequest(
            Payment payment, PaymentRequest request) 
    {
            boolean sameOrder = payment.getOrderId().equals(request.orderId());
            boolean sameAmount= payment.getAmount().compareTo(request.amount()) == 0;

            if(!sameOrder || !sameAmount)
            {
                throw new IdempotencyKeyConflictException("The idempotency key '"+ payment.getIdempotencyKey()
                + "' was already used for a different payment request.");
            
            }
    }


    private void validateInput(String idempotencyKey, PaymentRequest request)
    {
        if(idempotencyKey == null || idempotencyKey.isBlank())
        {
            throw new IllegalArgumentException("Idempotency-Key header is required.");
        }
        if(request == null)
        {
            throw new IllegalArgumentException("Payment request cannot be null.");
        }

        if(request.orderId() == null)
        {
            throw new IllegalArgumentException("Order ID is required.");
        }

        if(request.amount() == null || request.amount().signum() <=0)
        {
            throw new IllegalArgumentException("Payment Amount must be greater than zero.");
        }
    }


   private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getTransactionId(),
                payment.getStatus()
                
        );
    }

   

}
