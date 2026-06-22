package com.deepti.ecommerce.payment.service;

import org.springframework.stereotype.Service;

import com.deepti.ecommerce.payment.dto.PaymentRequest;
import com.deepti.ecommerce.payment.dto.PaymentResponse;
import com.deepti.ecommerce.payment.entity.Payment;
import com.deepti.ecommerce.payment.entity.PaymentStatus;
import com.deepti.ecommerce.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentResponse processPayment(PaymentRequest request)
    {
        boolean success = Math.random() > 0.2; //to simulate payment gateway processing success rate of 80%
        Payment payment = Payment.builder().orderId(request.orderId()).amount(request.amount())
                          .paymentMethod(request.paymentMethod())
                          .status(success?PaymentStatus.SUCCESS:PaymentStatus.FAILED)
                          .build();  

        Payment savedPayment = paymentRepository.save(payment);
        return mapToResponse(savedPayment);

    }

    public PaymentResponse getPaymentByOrderId(Long orderId)
    {
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(()-> 
                          new RuntimeException("Payment not found for Order Id:"+ orderId)
                         );

        return mapToResponse(payment);

    }
    
    private PaymentResponse mapToResponse(Payment payment)
    {
        return new PaymentResponse(payment.getId(),
        payment.getOrderId(),
        payment.getAmount(),
        payment.getTransactionId(),
        payment.getStatus());
    
    }

}
