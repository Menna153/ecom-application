package com.app.ecom_application.controller;

import com.app.ecom_application.dto.RefreshTokenRequest;
import com.app.ecom_application.model.LoginRequest;
import com.app.ecom_application.model.LoginResponse;
import com.app.ecom_application.model.User;
import com.app.ecom_application.repository.UserRepository;
import com.app.ecom_application.security.JwtService;
import com.app.ecom_application.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        String token = jwtService.generateToken(request.getUsername());

        String refreshToken =
                jwtService.generateRefreshToken(request.getUsername());

        refreshTokenService.saveRefreshToken(user, refreshToken);

        return ResponseEntity.ok(
                new LoginResponse(token, refreshToken)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @Valid @RequestBody RefreshTokenRequest request) {

        refreshTokenService.deleteByToken(request.getRefreshToken());

        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        if (!refreshTokenService.isValid(request.getRefreshToken())) {
            throw new IllegalArgumentException("INVALID_TOKEN");
        }

        String username =
                jwtService.extractUsername(request.getRefreshToken());

        if (!jwtService.isTokenValid(request.getRefreshToken(), username)) {
            throw new IllegalArgumentException("INVALID_TOKEN");
        }

        String accessToken = jwtService.generateToken(username);
        String refreshToken = jwtService.generateRefreshToken(username);

        User user = userRepository.findByUsername(username)
                .orElseThrow();

        refreshTokenService.saveRefreshToken(user, refreshToken);

        return ResponseEntity.ok(
                new LoginResponse(accessToken, refreshToken)
        );
    }
}
