package com.valeria.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties
{
    private String secretKey;
    private long accessTokenExpirationMs = 900000; // 15 минут
    private long refreshTokenExpirationMs = 604800000; // 7 дней
}
