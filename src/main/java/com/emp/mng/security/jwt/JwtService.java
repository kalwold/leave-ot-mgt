package com.emp.mng.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expiry;

    private SecretKey getSigningKey(){
       byte[] keyBytes = Decoders.BASE64.decode(secret);
       return Keys.hmacShaKeyFor(keyBytes);

    }

    public String generateToken(Long userId, String username, String roleCode, boolean mustChangePassword){
        Date now = new Date();
        Date Expiration =  new Date(now.getTime() + expiry);

        return Jwts.builder()
                .claims(Map.of(
                        "userId", userId,
                        "role",roleCode,
                        "mustChangePassword",mustChangePassword
                ))
                .subject(username)
                .issuedAt(now)
                .expiration(Expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token){
        return  extractAllClaims(token).getSubject();
    }

public Claims extractAllClaims(String token){
    return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
}
public boolean isTokenValid(String token) {
    try {
        extractAllClaims(token);
        return true;
    } catch (Exception exception) {
        return false;
    }
}
}