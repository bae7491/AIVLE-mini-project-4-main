package com.example.back.jwt;

import java.util.Date;
import java.security.Key;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final long ACCESS_TOKEN_EXP;
    private final long REFRESH_TOKEN_EXP;
    private final Key SECRET_KEY;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExp,
            @Value("${jwt.refresh-expiration}") long refreshExp
    ) {
        this.ACCESS_TOKEN_EXP = accessExp;
        this.REFRESH_TOKEN_EXP = refreshExp;

        this.SECRET_KEY = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // JWT 생성
    public String createAccessToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXP))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXP))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT에서 userId 추출
    public String getUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // JWT 유효성 검사
    public String validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);

            return "VALID";

        } catch (ExpiredJwtException e) {
            throw new RuntimeException("토큰이 만료되었습니다.");
        } catch (io.jsonwebtoken.security.SecurityException e) {
            throw new RuntimeException("토큰 서명 오류입니다.");
        } catch (MalformedJwtException e) {
            throw new RuntimeException("잘못된 토큰 형식입니다.");
        } catch (Exception e) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }
    }

    // Refresh Token 검증
    public void validateRefreshToken(String refreshToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(refreshToken);

        } catch (ExpiredJwtException e) {
            throw new RuntimeException("리프레시 토큰이 만료되었습니다.");
        } catch (SecurityException e) {
            throw new RuntimeException("리프레시 토큰 서명 오류입니다.");
        } catch (MalformedJwtException e) {
            throw new RuntimeException("잘못된 리프레시 토큰 형식입니다.");
        } catch (Exception e) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }
    }
}
