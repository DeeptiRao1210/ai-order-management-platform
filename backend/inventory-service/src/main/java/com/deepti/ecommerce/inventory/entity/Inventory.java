package com.deepti.ecommerce.inventory.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false,unique = true)
private Long productId;

@Column(nullable = false)
private Integer availableQuantity;

@Column(nullable = false)
private Integer reservedQuantity;

@Version
private Long version;

private LocalDateTime createdAt;
private LocalDateTime updatedAt;

@PrePersist
public void OnCreate()
{
    if( reservedQuantity == null)
    {
        reservedQuantity = 0;

    }
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();

}

@PreUpdate
public void OnUpdate()
{
    updatedAt = LocalDateTime.now();
}



}
