package com.deepti.ecommerce.order.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
 @NotNull Long orderId,
   
   @NotNull 
   @DecimalMin("0.1")
   BigDecimal amount,

   @NotBlank
   String paymentMethod


) {

}
