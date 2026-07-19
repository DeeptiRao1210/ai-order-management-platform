package com.deepti.ecommerce.payment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments", uniqueConstraints = {
    @UniqueConstraint(name = "unique_payment_idempotency_key",columnNames = "idempotency_key")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, updatable = false)
    private String idempotencyKey;

    private Long orderId;

    private BigDecimal amount;
    
    private String paymentMethod;

    private String transactionId;

    private String failureReason;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate()
    {
        createdAt = LocalDateTime.now();
        if(transactionId ==null)
        {
            transactionId = UUID.randomUUID().toString();
        }

        if(status ==null)
        {
            status = PaymentStatus.PENDING;
        }
    }


}
