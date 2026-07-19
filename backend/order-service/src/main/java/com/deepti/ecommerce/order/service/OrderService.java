package com.deepti.ecommerce.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deepti.ecommerce.order.client.InventoryClient;
import com.deepti.ecommerce.order.client.PaymentClient;
import com.deepti.ecommerce.order.dto.OrderRequest;
import com.deepti.ecommerce.order.dto.OrderResponse;
import com.deepti.ecommerce.order.dto.PaymentRequest;
import com.deepti.ecommerce.order.dto.PaymentResponse;
import com.deepti.ecommerce.order.dto.ReserveInventoryRequest;
import com.deepti.ecommerce.order.entity.Order;
import com.deepti.ecommerce.order.entity.OrderItem;
import com.deepti.ecommerce.order.entity.OrderStatus;
import com.deepti.ecommerce.order.exception.PaymentRequestConflictException;
import com.deepti.ecommerce.order.repository.OrderRepository;
import com.deepti.ecommerce.order.service.integration.InventoryServiceGateway;
import com.deepti.ecommerce.order.service.integration.PaymentServiceGateway;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Service

public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryServiceGateway inventoryServiceGateway;
    private final PaymentServiceGateway paymentServiceGateway;

    public OrderService(OrderRepository orderRepository, InventoryServiceGateway inventoryServiceGateway,PaymentServiceGateway paymentServiceGateway)
    {
        this.orderRepository = orderRepository;
        this.inventoryServiceGateway = inventoryServiceGateway;
        this.paymentServiceGateway = paymentServiceGateway;
    }




    @Transactional
    public OrderResponse createOrder(OrderRequest request)
    {
        BigDecimal totalAmount= (BigDecimal) request.items().stream()
                                .map(item->item.price().multiply(BigDecimal.valueOf(item.quantity())))
                               .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder().userId(request.userId())
                      .totalAmount(totalAmount).status(OrderStatus.PENDING)
                      .build();

        List<OrderItem> orderItems = request.items()
                                     .stream()
                                     .map(item->OrderItem.builder()
                                          .productId(item.productId())
                                          .quantity(item.quantity())
                                          .price(item.price())
                                          .order(order)
                                          .build()).collect(Collectors.toCollection(ArrayList::new));

        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        String idempotencyKey =
        "ORDER-PAYMENT-" + savedOrder.getId();



        List<OrderItem> reservedItems = new ArrayList<>();
        try{
             reserveAllInventory(savedOrder, reservedItems);
             savedOrder.setStatus(OrderStatus.INVENTORY_RESERVED);
             orderRepository.save(savedOrder);
             
             PaymentResponse paymentResponse = paymentServiceGateway.processPayment(idempotencyKey,
              new PaymentRequest(savedOrder.getId(), savedOrder.getTotalAmount(), "CARD")
            );
            
            return handlePaymentResult(savedOrder,reservedItems, paymentResponse);
             


        }
        catch(Exception ex)
        {
            compensateInventoryReservations(reservedItems);
            savedOrder.setStatus(OrderStatus.FAILED);
            orderRepository.save(savedOrder);
             if (ex instanceof PaymentRequestConflictException
            conflictException) 
            {

                throw conflictException;
            }

            if (ex instanceof IllegalArgumentException
            illegalArgumentException)
            {

                throw illegalArgumentException;
            }    



            throw new RuntimeException("Order failed. Saga compensation initiated. Reason: "
                            + getExceptionMessage(ex), ex);
            
        }
     



    }

    private void reserveAllInventory(Order savedOrder, List<OrderItem> reservedItems)
    {
        for(OrderItem item: savedOrder.getItems())
        {
            ReserveInventoryRequest inventoryRequest = new ReserveInventoryRequest(
                                                        item.getProductId(),
                                                        item.getQuantity());

            inventoryServiceGateway.reserveInventory(inventoryRequest);
            reservedItems.add(item);

        }
    }

    private OrderResponse handlePaymentResult(Order savedOrder, List<OrderItem> reservedItems, PaymentResponse paymentResponse)
    {
        if(paymentResponse == null)
        {
            throw new IllegalStateException("Payment service returned an empty response.");
        }

        String paymentStatus = paymentResponse.status();
        if("SUCCESS".equalsIgnoreCase(paymentStatus))
        {
            confirmAllInventory(reservedItems);
            savedOrder.setStatus(OrderStatus.CONFIRMED);
            Order confirmedOrder = orderRepository.save(savedOrder);
            return mapToResponse(confirmedOrder);
        }

        if("PENDING".equalsIgnoreCase(paymentStatus) || "UNKNOWN".equalsIgnoreCase(paymentStatus) ||"PROCESSING".equalsIgnoreCase(paymentStatus))
        {
            savedOrder.setStatus(OrderStatus.PAYMENT_PENDING);
            Order pendingOrder = orderRepository.save(savedOrder);
            return mapToResponse(pendingOrder);
        }
        compensateInventoryReservations(reservedItems);
        savedOrder.setStatus(OrderStatus.FAILED);
        Order failedOrder = orderRepository.save(savedOrder);
        return mapToResponse(failedOrder);

    }

    private void confirmAllInventory(List<OrderItem> reservedItems)
    {
        for(OrderItem item: reservedItems)
        {
            inventoryServiceGateway.confirmInventory(new ReserveInventoryRequest(item.getProductId(),
                                                      item.getQuantity()));                                                                            
        }
    }


    private void compensateInventoryReservations(List<OrderItem> reservedItems)
    {
        for(OrderItem reservedItem: reservedItems)
        {
            try{
                inventoryServiceGateway.releaseInventory(new ReserveInventoryRequest(reservedItem.getProductId(),
                                                        reservedItem.getQuantity()));
            }
            catch(Exception compensationException)
            {
                 System.err.println(
                        "Inventory release failed for productId="
                                + reservedItem.getProductId()
                                + ", reason="
                                + getExceptionMessage(compensationException)
                );
            }
        }
    }




    public OrderResponse getOrderById(Long id)
    {
        Order order = orderRepository.findById(id)
                      .orElseThrow(()->new RuntimeException("Order not found with id: "+id));

        return mapToResponse(order);              
    }

    public List<OrderResponse> getOrdersByUserId(Long userId)
    {
        return orderRepository.findByUserId(userId).stream()
               .map(this::mapToResponse).toList();
    }


    public OrderResponse cancelOrder(Long id)
    {
        Order order = orderRepository.findById(id)
                        .orElseThrow(()-> new RuntimeException("Order not found with id: "+id));

        if(order.getStatus() == OrderStatus.CONFIRMED)
        {
            throw new IllegalStateException("A confirmed order cannot be cancelled directly.");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return mapToResponse(orderRepository.save(order)) ;               
    }



    private OrderResponse mapToResponse(Order order)
    {
        return new OrderResponse(order.getId(),
                                 order.getUserId(),
                                 order.getTotalAmount(),
                                 order.getStatus(),
                                 order.getCreatedAt() );
    }


    private String getExceptionMessage(Throwable throwable) {

        return throwable.getMessage() != null
                ? throwable.getMessage()
                : throwable.getClass().getSimpleName();
    }

}
