package com.app.ecom_application.service;

import com.app.ecom_application.model.LoginRequest;
import com.app.ecom_application.model.LoginResponse;
import com.app.ecom_application.model.User;
import com.app.ecom_application.repository.UserRepository;
import com.app.ecom_application.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new IllegalArgumentException("USER_NOT_FOUND"));

        String accessToken = jwtService.generateToken(user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        refreshTokenService.deleteByUser(user);

        refreshTokenService.saveRefreshToken(user, refreshToken);

        return new LoginResponse(accessToken, refreshToken);
    }

    public LoginResponse refreshToken(String refreshToken) {

        if (!refreshTokenService.isValid(refreshToken)) {
            throw new IllegalArgumentException("INVALID_TOKEN");
        }

        String username = jwtService.extractUsername(refreshToken);

        if (!jwtService.isTokenValid(refreshToken, username)
                || !jwtService.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("INVALID_TOKEN");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("USER_NOT_FOUND"));

        // Invalidate old refresh token
        refreshTokenService.deleteByToken(refreshToken);

        String newAccessToken = jwtService.generateToken(username);
        String newRefreshToken = jwtService.generateRefreshToken(username);

        refreshTokenService.saveRefreshToken(user, newRefreshToken);

        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    public void logout(String refreshToken) {

        if (!refreshTokenService.isValid(refreshToken)) {
            throw new IllegalArgumentException("INVALID_TOKEN");
        }

        refreshTokenService.deleteByToken(refreshToken);
    }
}
