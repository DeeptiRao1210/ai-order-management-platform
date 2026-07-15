package com.deepti.ecommerce.order.service.integration;

import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

import com.deepti.ecommerce.order.client.InventoryClient;

import com.deepti.ecommerce.order.dto.InventoryResponse;
import com.deepti.ecommerce.order.dto.ReserveInventoryRequest;
import com.deepti.ecommerce.order.exception.InventoryServiceUnavailableException;

@Service
public class InventoryServiceGateway {

    private final InventoryClient inventoryClient;
    private final CircuitBreakerFactory<?,?> circuitBreakerFactory;
    
    public InventoryServiceGateway(InventoryClient invClient, CircuitBreakerFactory<?,?> cirBreakerFactory)
    {
        this.inventoryClient = invClient;
        this.circuitBreakerFactory = cirBreakerFactory;
    }

    public InventoryResponse reserveInventory(ReserveInventoryRequest request)
    {
        CircuitBreaker circuitBreaker =  circuitBreakerFactory.create("inventoryServiceCircuitBreaker");
        return circuitBreaker.run(
                ()-> inventoryClient.reserveInventory(request),
                throwable->inventoryFallback(request, throwable)
            );
    }

    public InventoryResponse confirmInventory(ReserveInventoryRequest request)
    {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("inventoryServiceCircuitBreaker");
        return circuitBreaker.run(()->inventoryClient.confirmInventory(request), throwable->{
            throw new InventoryServiceUnavailableException("Inventory confirmation failed", throwable);
        });
    }

    public InventoryResponse releaseInventory(ReserveInventoryRequest request) 
    {

    CircuitBreaker circuitBreaker = circuitBreakerFactory.create("inventoryServiceCircuitBreaker");

    return circuitBreaker.run(
            () -> inventoryClient.releaseInventory(request),
            throwable -> {
                throw new InventoryServiceUnavailableException(
                        "Inventory release failed",
                        throwable
                );
            }
    );
}



    private InventoryResponse inventoryFallback(ReserveInventoryRequest request, Throwable  throwable)
    {
          System.err.println(
                "Inventory Service call failed for productId "
                        + request.productId()
                        + ": "
                        + throwable.getMessage()
        );
        
        
        return new InventoryResponse(null,
                                    request.productId(),
                                    null,
                                    null 
                            );
        
    }

}
