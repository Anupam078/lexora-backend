package com.lexora.lexora_backend.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET_KEY = "AojhaSecretKeyLexora078ForDistrictCourtManagement";

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
                Base64.getEncoder().encodeToString(SECRET_KEY.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }
    private static final long EXPIRATION_TIME= 1000*60*60*24;

    public String generateToken(String email, String tenantId, String role) {
        return Jwts.builder()
                .claim("userId", email)
                .claim("tenantId", tenantId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired");

        } catch (SignatureException e) {
            System.out.println("Invalid signature");

        } catch (MalformedJwtException e) {
            System.out.println("Malformed token");

        } catch (Exception e) {
            System.out.println("Invalid token");
        }

        return false;
    }
}

