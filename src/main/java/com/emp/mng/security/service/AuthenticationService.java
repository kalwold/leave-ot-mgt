package com.emp.mng.security.service;


import com.emp.mng.security.jwt.*;
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
import java.util.List;

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
        // Collect all assigned role codes
        List<String> roles = userRoles.stream()
                .map(userRole -> userRole.getRole().getRoleCode())
                .distinct()
                .toList();

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
        String accessToken  = jwtService.generateToken(user.getId(), user.getUsername(), roles,user.getMustChangePassword(), newSessionVersion);

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

    return new LoginResponse(accessToken,refreshToken, "Bearer", expiration, user.getId(), user.getUsername(), user.getEmployee().getFullName(), roles, Boolean.TRUE.equals(user.getMustChangePassword()));

    }

    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        // Hash the submitted refresh token
        String submittedTokenHash =
                refreshTokenService.hashToken(request.getRefreshToken());
        // Find the user using the token hash
        User user = userRepository.findByRefreshTokenHash(submittedTokenHash)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid refresh token"
                ));
        // Check account status
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your account is inactive"
            );
        }
        // Check token expiration
        OffsetDateTime now = OffsetDateTime.now(
                ZoneId.of("Africa/Addis_Ababa")
        );

        if (user.getRefreshTokenExpiresAt() == null ||
                !user.getRefreshTokenExpiresAt().isAfter(now)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token has expired. Please log in again."
            );
        }

        // Get all assigned roles
        var userRoles = userRoleRepository.findAllByUser_Id(user.getId());

        if (userRoles.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No role assigned to this account"
            );
        }

        List<String> roles = userRoles.stream()
                .map(userRole -> userRole.getRole().getRoleCode())
                .distinct()
                .toList();

        // Generate a new access token using the existing session version
        boolean mustChangePassword =
                Boolean.TRUE.equals(user.getMustChangePassword());

        String accessToken = jwtService.generateToken(
                user.getId(),
                user.getUsername(),
                roles,
                mustChangePassword,
                user.getSessionVersion()
        );

        // Rotate the refresh token
        String newRefreshToken =
                refreshTokenService.generateRefreshToken();

        user.setRefreshTokenHash(
                refreshTokenService.hashToken(newRefreshToken)
        );

        user.setRefreshTokenExpiresAt(
                now.plusDays(REFRESH_TOKEN_EXPIRATION_DAYS)
        );

        userRepository.save(user);

        return new RefreshTokenResponse(
                accessToken,
                newRefreshToken,
                "Bearer",
                expiration
        );
    }

    @Transactional
    public LogoutResponse logout(String username) {

        // Find the authenticated user
        User existingUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        User user = userRepository.findByIdForUpdate(existingUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found"));
        //Invalidate the refresh token
        user.setRefreshTokenHash(null);
        user.setRefreshTokenExpiresAt(null);

        // Invalidate all previously issued access tokens
        int currentVersion = user.getSessionVersion() == null
                ? 0
                : user.getSessionVersion();

        user.setSessionVersion(currentVersion + 1);

        userRepository.save(user);
        return new LogoutResponse("You have been logged out successfully.");

    }

    }
