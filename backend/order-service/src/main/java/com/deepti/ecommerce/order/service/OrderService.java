package com.deepti.ecommerce.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deepti.ecommerce.order.client.InventoryClient;
import com.deepti.ecommerce.order.dto.OrderRequest;
import com.deepti.ecommerce.order.dto.OrderResponse;
import com.deepti.ecommerce.order.dto.ReserveInventoryRequest;
import com.deepti.ecommerce.order.entity.Order;
import com.deepti.ecommerce.order.entity.OrderItem;
import com.deepti.ecommerce.order.entity.OrderStatus;
import com.deepti.ecommerce.order.repository.OrderRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

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
        List<OrderItem> reservedItems = new ArrayList<>();
        try{
               for(OrderItem item: savedOrder.getItems())
                {
                    inventoryClient.reserveInventory(new ReserveInventoryRequest(
                        item.getProductId(), 
                        item.getQuantity()));

                     reservedItems.add(item);       
                } 

                savedOrder.setStatus(OrderStatus.INVENTORY_RESERVED);
                Order updatedOrder  = orderRepository.save(savedOrder);
                
                return mapToResponse(updatedOrder);

        }
        catch(FeignException ex)
        {
            for(OrderItem reservedItem: reservedItems)
            {
                try{
                        inventoryClient.releaseInventory(new ReserveInventoryRequest(reservedItem.getProductId()
                    , reservedItem.getQuantity()));

                }
                catch(Exception releaseException)
                {
                    //send to Kafka
                }
            }
            savedOrder.setStatus(OrderStatus.FAILED);
            orderRepository.save(savedOrder);
            throw new RuntimeException("Order failed due to inventory-service error:"+ ex.getMessage());


        }
        catch(Exception ex)
        {
            savedOrder.setStatus(OrderStatus.FAILED);
            orderRepository.save(savedOrder);
            throw new RuntimeException("Order Creation failed: "+ ex.getMessage());
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


}
