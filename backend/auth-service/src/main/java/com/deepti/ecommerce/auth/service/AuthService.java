package com.deepti.ecommerce.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.deepti.ecommerce.auth.dto.AuthResponse;
import com.deepti.ecommerce.auth.dto.LoginRequest;
import com.deepti.ecommerce.auth.dto.RegisterRequest;
import com.deepti.ecommerce.auth.entity.AppUser;
import com.deepti.ecommerce.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    public AuthResponse register(RegisterRequest request)
    {
        if(userRepository.findByEmail(request.email()).isPresent())
        {
            throw new RuntimeException("Email already Registered.");
        }
        AppUser user = AppUser.builder().fullName(request.fullName())
                               .email(request.email())
                               .pasword(passwordEncoder.encode(request.password()))
                               .role(request.role())
                               .build(); 

        userRepository.save(user);
        String token = jwtService.generateToken(user.getEmail(),user.getRole());
        return new AuthResponse(token,user.getEmail(),user.getRole());
    }


    public AuthResponse login(LoginRequest request)
    {
        AppUser user = userRepository.findByEmail(request.email())
                                    .orElseThrow(()->new RuntimeException("Invalid email or password."));
   
        if(!passwordEncoder.matches(request.password(),user.getPasword()))
        {
            throw new RuntimeException("Invalid email or password.");

        }
        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token,user.getEmail(),user.getRole());
   
    }


}
