package com.example.taskmanager.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    public static final String JWT_COOKIE_NAME = "user_auth_token";
    public static final String JWT_COOKIE_PATH = "/";
    public static final int JWT_COOKIE_MAX_AGE = 24 * 60 * 60; // 1 day
    public static final boolean JWT_COOKIE_SECURE = true;
    public static final String JWT_COOKIE_SAME_SITE = "Strict";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public ResponseCookie generateJwtCookie(String token) {
        return ResponseCookie.from(JWT_COOKIE_NAME, token)
                .httpOnly(true)
                .path(JWT_COOKIE_PATH)
                .secure(JWT_COOKIE_SECURE)
                .maxAge(JWT_COOKIE_MAX_AGE)
                .sameSite(JWT_COOKIE_SAME_SITE)
                .build();
    }

    public ResponseCookie getCleanJwtCookie() {
        SecurityContextHolder.clearContext();
        ResponseCookie jwtCookie = ResponseCookie.from(JWT_COOKIE_NAME, "")
                .path(JWT_COOKIE_PATH)
                .maxAge(0)
                .httpOnly(true)
                .secure(JWT_COOKIE_SECURE)
                .sameSite(JWT_COOKIE_SAME_SITE)
                .build();
        return jwtCookie;
    }
}