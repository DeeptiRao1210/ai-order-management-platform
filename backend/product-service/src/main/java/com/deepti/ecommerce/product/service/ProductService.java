package com.deepti.ecommerce.product.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.deepti.ecommerce.product.dto.ProductRequest;
import com.deepti.ecommerce.product.dto.ProductResponse;
import com.deepti.ecommerce.product.entity.Product;
import com.deepti.ecommerce.product.repository.ProductRepository;


import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(ProductRequest request)
    {
        Product product = Product.builder().name(request.name()).description(request.description())
                                           .price(request.price()).category(request.category()).active(true).build();
         
         return mapToResponse(productRepository.save(product));                                   
    }

    public List<ProductResponse> getAllProducts()
    {
     return  productRepository.findAll().stream().map(this::mapToResponse)
                              .toList();
                          
    }

    public ProductResponse getProductById(Long id)
    {
        Product product = productRepository.findById(id)
                         .orElseThrow(()->new RuntimeException("Product with id" + id + " not found"));

           return  mapToResponse(product);
    }

     public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(request.category());

        return mapToResponse(productRepository.save(product));
    }

    public void deleteProduct(Long id)
    {
        Product product = productRepository.findById(id).orElseThrow(()->new RuntimeException("Product with id" + id + " not found"));
        product.setActive(false);
        productRepository.save(product);
    }

    public List<ProductResponse> searchByName(String name)
    {
        return productRepository.findByNameContainingIgnoreCase(name)
               .stream().map(this::mapToResponse).toList();

    }

    public ProductResponse mapToResponse(Product product)
    {
        return new ProductResponse(product.getId(), product.getName(),product.getDescription(),
                product.getPrice(), product.getCategory(),product.getActive(),
                product.getCreatedAt(),product.getUpdatedAt());
    }
}
