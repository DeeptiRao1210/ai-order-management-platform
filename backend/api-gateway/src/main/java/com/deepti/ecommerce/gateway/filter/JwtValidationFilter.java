package com.deepti.ecommerce.gateway.filter;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Component
public class JwtValidationFilter implements GlobalFilter, Ordered{

    @Value("${jwt.secret}")
    private String secret;


    private SecretKey getSigningKey()
    {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain)
    {
        String path = exchange.getRequest().getURI().getPath();

        if(path.startsWith("/api/auth"))
        {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer"))
        {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        try
        {
              String token = authHeader.substring(7);
              Claims claims = Jwts.parser()
                                  .verifyWith(getSigningKey())
                                  .build()
                                  .parseSignedClaims(token)
                                  .getPayload(); 

             String email = claims.getSubject();
             String role = claims.get("role", String.class);

            if (path.startsWith("/api/inventory") && !"ADMIN".equals(role))
             {
                 exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
             }

            if (path.startsWith("/api/payments") && !"CUSTOMER".equals(role))
            {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            
            if (path.startsWith("/api/products")
                && !exchange.getRequest().getMethod().name().equals("GET")
                && !"ADMIN".equals(role)) 
            {

                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }




             ServerWebExchange mutatedExchange = exchange.mutate()
                                                          .request(builder-> builder
                                                                             .header("X-User-Email",email)
                                                                             .header("X-User-Role", role)               
                                                          ).build();       
            return chain.filter(mutatedExchange);

        }
        catch(Exception ex)
        {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

    }

    @Override
    public int getOrder()
    {
        return -1;
    }




}
