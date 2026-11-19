package com.emp.mng.security.service;


import com.emp.mng.security.jwt.JwtService;
import com.emp.mng.security.jwt.LoginRequest;
import com.emp.mng.security.jwt.LoginResponse;
import com.emp.mng.user.entity.User;
import com.emp.mng.user.repository.UserRepository;
import com.emp.mng.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private  final RefreshTokenService refreshTokenService;

    @Value("${app.jwt.expiration}")
    private long expiration;

    private static final long REFRESH_TOKEN_EXPIRATION_DAYS = 1;

    @Transactional
    public LoginResponse login(LoginRequest request){
        String username = request.getUsername().trim().toLowerCase();

        // Find user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        // Check account status
        if(!Boolean.TRUE.equals(user.getActive())){
            throw  new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account is inactive. Contact the administrator.");
        }
        // Verify password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        // Find assigned role
        var userRoles = userRoleRepository.findAllByUser_Id(user.getId());

        if (userRoles.isEmpty()){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"No role assigned to this account");
        }
        // Use the first assigned role for the current login response
        String roleCode = userRoles.get(0).getRole().getRoleCode();

        // Increment session version.
        // This invalidates access tokens from previous logins.
        int newSessionVersion =
                (user.getSessionVersion() == null
                        ? 0
                        : user.getSessionVersion()) + 1;

        user.setSessionVersion(newSessionVersion);
        boolean mustChangePassword =
                Boolean.TRUE.equals(user.getMustChangePassword());
        // Generate new access token
        String accessToken  = jwtService.generateToken(user.getId(), user.getUsername(), roleCode,user.getMustChangePassword(), newSessionVersion);

        // Generate refresh token
        String refreshToken =
                refreshTokenService.generateRefreshToken();
        user.setRefreshTokenHash(
                refreshTokenService.hashToken(refreshToken)
        );
        // Set refresh token expiry to 7 days
        OffsetDateTime now = OffsetDateTime.now(
                ZoneId.of("Africa/Addis_Ababa")
        );

        user.setRefreshTokenExpiresAt(
                now.plusDays(REFRESH_TOKEN_EXPIRATION_DAYS)
        );
        //Update last login
        user.setLastLoginAt(now);
    userRepository.save(user);

    return new LoginResponse(accessToken,refreshToken, "Bearer", expiration, user.getId(), user.getUsername(), user.getEmployee().getFullName(), roleCode, Boolean.TRUE.equals(user.getMustChangePassword()));

    }
}
