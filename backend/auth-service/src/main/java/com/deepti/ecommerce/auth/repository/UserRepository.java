package com.deepti.ecommerce.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deepti.ecommerce.auth.entity.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Long>{

    Optional<AppUser> findByEmail(String email);

}
