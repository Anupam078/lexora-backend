package com.lexora.lexora_backend.auth;

import com.lexora.lexora_backend.tenant.TenantContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    public JwtAuthenticationFilter(JwtService jwtService){
        this.jwtService=jwtService;
    }
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {
        String authHeader= request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            chain.doFilter(request,response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.validateToken(token)) {
            chain.doFilter(request, response);
            return;
        }

        Claims claims = jwtService.extractClaims(token);
        String userId = claims.get("userId", String.class);
        String tenantId = claims.get("tenantId", String.class);
        String role = claims.get("role", String.class);

        TenantContext.setTenantId(tenantId);

        // STEP 5 — Tell Spring Security this request is authenticated
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
    }
}
