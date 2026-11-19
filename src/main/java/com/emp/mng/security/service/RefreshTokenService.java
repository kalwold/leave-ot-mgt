package com.emp.mng.security.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class RefreshTokenService {
    private  final SecureRandom secureRandom = new SecureRandom();

    public String generateRefreshToken(){
        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);
        return Base64.getMimeEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

    }

    public String hashToken(String token){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8)

            );
            return  Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException( "Could not hash refresh token", e);
        }

    }
}
