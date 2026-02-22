package com.mekheainteractive.authenticator_service.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${JwtSecretKey}")
    private String SECRET_KEY;

    // Generate a JWT token for a PlayFabId
    public String generateToken(String playfabId) {
        return Jwts
                .builder()
                .subject(playfabId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 43200000)) // 12h
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    // Extract the PlayFabId from a JWT token
    public String extractPlayfabId(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Validate JWT token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Generate a JWT token for a PlayFabId
    public String generateServiceToken() {
        return Jwts
                .builder()
                .subject("notification-service")
                .claim("role", "service")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 43200000)) // 12h
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
}
