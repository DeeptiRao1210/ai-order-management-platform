package com.deepti.ecommerce.auth.dto;

public record AuthResponse(

    String token,
    String email, 
    String role

) {

}
