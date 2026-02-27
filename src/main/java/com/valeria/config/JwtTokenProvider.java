package com.valeria.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
    }

    // Генерация Access Token
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities());
        claims.put("type", "ACCESS");

        return buildToken(claims, userDetails.getUsername(), jwtProperties.getAccessTokenExpirationMs());
    }

    // Генерация Refresh Token
    public String generateRefreshToken(UserDetails userDetails, UUID sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sessionId", sessionId.toString());
        claims.put("type", "REFRESH");

        return buildToken(claims, userDetails.getUsername(), jwtProperties.getRefreshTokenExpirationMs());
    }

    private String buildToken(Map<String, Object> claims, String subject, long expirationMs) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(expirationMs)))
                .signWith(getSecretKey())
                .compact();
    }

    // Извлечение username из токена
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Извлечение типа токена
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    // Извлечение ID сессии из Refresh Token
    public UUID extractSessionId(String token) {
        String sessionId = extractClaim(token, claims -> claims.get("sessionId", String.class));
        return sessionId != null ? UUID.fromString(sessionId) : null;
    }

    // Проверка валидности токена для пользователя
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
