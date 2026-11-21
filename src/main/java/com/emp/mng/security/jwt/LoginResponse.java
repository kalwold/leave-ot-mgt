package com.emp.mng.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private Long userId;
    private String username;
    private String employeeName;
    private List<String> roles;

    private boolean mustChangePassword;
}



