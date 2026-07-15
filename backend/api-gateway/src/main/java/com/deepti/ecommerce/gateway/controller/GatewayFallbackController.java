package com.deepti.ecommerce.gateway.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
public class GatewayFallbackController {

    @RequestMapping("/fallback/products")
    public Mono<ResponseEntity<Map<String, Object>>> productFallback(ServerWebExchange exchange)
    {
        return serviceUnavailable("PRODUCT-SERVICE", exchange.getRequest().
                                                    getPath().value());
    }


     @RequestMapping("/fallback/orders")
    public Mono<ResponseEntity<Map<String, Object>>> orderFallback(ServerWebExchange exchange)
    {
        return serviceUnavailable("ORDER-SERVICE", exchange.getRequest().
                                                    getPath().value());
    }

    @RequestMapping("/fallback/inventory")
    public Mono<ResponseEntity<Map<String, Object>>> inventoryFallback(ServerWebExchange exchange)
    {
        return serviceUnavailable("INVENTORY-SERVICE", exchange.getRequest().
                                                    getPath().value());
    }
    
     @RequestMapping("/fallback/payments")
    public Mono<ResponseEntity<Map<String, Object>>> paymentFallback(ServerWebExchange exchange)
    {
        return serviceUnavailable("PAYMENT-SERVICE", exchange.getRequest().
                                                    getPath().value());
    }


    private Mono<ResponseEntity<Map<String,Object>>> serviceUnavailable(String service, String path)
    {
        Map<String, Object> body = Map.of(
                                    "timestamp", Instant.now().toString(),
                                    "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                                    "error","Service Unavailable",
                                    "service", service,
                                    "message", service+" is temporarily unavailable. Please try again later.",
                                    "path", path);

            return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                             .body(body)   );
    }
}
