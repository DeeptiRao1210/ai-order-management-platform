package com.deepti.ecommerce.inventory.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deepti.ecommerce.inventory.dto.InventoryRequest;
import com.deepti.ecommerce.inventory.dto.InventoryResponse;
import com.deepti.ecommerce.inventory.dto.ReserveInventoryRequest;
import com.deepti.ecommerce.inventory.service.InventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public InventoryResponse createInventory(@Valid @RequestBody InventoryRequest request)
    {
       
      return inventoryService.createInventory(request); 
       
    }

    @GetMapping
    public List<InventoryResponse> getAllInventory() {
       return inventoryService.getAllInventory();
    }
    
    @GetMapping("/{productId}")
    public InventoryResponse getInventoryByProductId(@PathVariable Long productId) {
        return inventoryService.getInventoryByProductId(productId);
    }
    
    @PostMapping("/reserve")
    public InventoryResponse reserveInventory(@Valid @RequestBody ReserveInventoryRequest request)
    {
        return inventoryService.reserveInventory(request);
    }


    @PostMapping("/release")
    public InventoryResponse releaseInventory( @Valid @RequestBody ReserveInventoryRequest request) {
        
        return inventoryService.releaseInventory(request);
    }
    
    @PutMapping("/{productId}/stock")
    public InventoryResponse updateStock(@PathVariable Long productId, @RequestParam Integer quantity)
    {
        return inventoryService.updateStock(productId, quantity);
    }

    @PostMapping("/confirm")
    public InventoryResponse confirmInventory(@Valid @RequestBody ReserveInventoryRequest request) {
      
       return inventoryService.confirmInventory(request);
    }
    

}
