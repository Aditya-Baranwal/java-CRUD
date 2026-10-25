package com.aditya.lms.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class TokenService {

    private final SecretKey key;
    private static final long EXPIRY_MILLIS = 60 * 60 * 1000L;

    public TokenService(@Value("${jwt.secret}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String generateToken(
            String userId, List<String> authorities) {

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(userId)
                .claim("authorities", authorities)
                .issuedAt(new Date(now))
                .expiration(new Date(now + EXPIRY_MILLIS))
                .signWith(key)
                .compact();
    }

    public long getExpirySeconds() {
        return EXPIRY_MILLIS / 1000;
    }

    /*{
        "sub": "usr_9f81a7b4c302",
        "authorities": [
                "ROLE_USER",
                "course:read",
                "course:write"
        ],
        "iat": 1718345123,
        "exp": 1718348723
    }*/
    public Claims decodeToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

