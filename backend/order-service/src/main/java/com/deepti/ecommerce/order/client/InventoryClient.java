package com.deepti.ecommerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.deepti.ecommerce.order.dto.InventoryResponse;
import com.deepti.ecommerce.order.dto.ReserveInventoryRequest;



@FeignClient(
name = "inventory-service",
url = "${inventory.service.url}"
)
public interface InventoryClient {

@PostMapping("/api/inventory/reserve")
InventoryResponse reserveInventory(@RequestBody ReserveInventoryRequest request);

@PostMapping("/api/inventory/release")
InventoryResponse releaseInventory(@RequestBody ReserveInventoryRequest request);

@GetMapping("/api/inventory/{productId}")
InventoryResponse  getInventoryByProductId(@PathVariable Long productId);

}
