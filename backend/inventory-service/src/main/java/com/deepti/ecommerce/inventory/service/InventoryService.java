package com.deepti.ecommerce.inventory.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import com.deepti.ecommerce.inventory.dto.InventoryRequest;
import com.deepti.ecommerce.inventory.dto.InventoryResponse;
import com.deepti.ecommerce.inventory.dto.ReserveInventoryRequest;
import com.deepti.ecommerce.inventory.entity.Inventory;
import com.deepti.ecommerce.inventory.exception.InsufficientInventoryException;
import com.deepti.ecommerce.inventory.exception.InventoryNotFoundException;
import com.deepti.ecommerce.inventory.repository.InventoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryResponse createInventory(InventoryRequest request)
    {
        Inventory inventory = Inventory.builder().productId(request.productId())
                              .availableQuantity(request.availableQuantity())
                              .reservedQuantity(0).build();

        return mapToResponse(inventoryRepository.save(inventory));                      
  
    }

    public List<InventoryResponse> getAllInventory()
    {
        return inventoryRepository.findAll().stream()
               .map(this::mapToResponse).toList();
    }


    public InventoryResponse getInventoryByProductId(Long productId)
    {
       Inventory inventory =  inventoryRepository.findByProductId(productId).orElseThrow(()->
                            new InventoryNotFoundException("Inventory not found for product with ID:"+ productId) );
            
        return mapToResponse(inventory);     
    }

    @Transactional
    public InventoryResponse reserveInventory(ReserveInventoryRequest request)
    {
        Inventory inventory =  inventoryRepository.findByProductId(request.productId()).orElseThrow(()->
                            new InventoryNotFoundException("Inventory not found for product with ID:"+ request.productId()) );

    if(inventory.getAvailableQuantity() < request.quantity())
    {
        throw new InsufficientInventoryException("Insufficient stock for product Id:"+ request.productId());
    }

    inventory.setAvailableQuantity(inventory.getAvailableQuantity()-request.quantity());
    inventory.setReservedQuantity(inventory.getReservedQuantity()+request.quantity());
    return mapToResponse(inventoryRepository.save(inventory));

     }

     @Transactional
     public InventoryResponse releaseInventory(ReserveInventoryRequest request)
     {

         Inventory inventory =  inventoryRepository.findByProductId(request.productId()).orElseThrow(()->
                            new InventoryNotFoundException("Inventory not found for product with ID:"+ request.productId()));

        if(inventory.getReservedQuantity() < request.quantity())
        {
            throw new InsufficientInventoryException("Reserved Quantity is less than release quantity.");
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity()+ request.quantity());
        inventory.setReservedQuantity(inventory.getReservedQuantity()-request.quantity());
    
        return mapToResponse(inventoryRepository.save(inventory));
    
    }

   public InventoryResponse updateStock(Long productId, Integer quantity)
   {
     Inventory inventory =  inventoryRepository.findByProductId(productId).orElseThrow(()->
                            new InventoryNotFoundException("Inventory not found for product with ID:"+ productId));

     inventory.setAvailableQuantity(quantity);
     return mapToResponse(inventoryRepository.save(inventory));
   }

   private InventoryResponse mapToResponse(Inventory inventory)
   {
        return new InventoryResponse(
        inventory.getId(), 
        inventory.getProductId(),
        inventory.getAvailableQuantity(),
        inventory.getReservedQuantity());

   }

   @Transactional
   public InventoryResponse confirmInventory(ReserveInventoryRequest request)
   {
        Inventory inventory = inventoryRepository.findByProductId(request.productId())
                              .orElseThrow(()->
                              new InventoryNotFoundException("Inventory not found for product :"+ request.productId()));

        if(inventory.getReservedQuantity() < request.quantity())
        {
            throw new InsufficientInventoryException("Reserved Quantity is less than confirm quantity");
        }  
        
        inventory.setReservedQuantity(inventory.getReservedQuantity()-request.quantity());

        return mapToResponse(inventoryRepository.save(inventory));
   }

}
