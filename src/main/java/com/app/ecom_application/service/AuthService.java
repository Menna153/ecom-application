package com.app.ecom_application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;

    public boolean login(String username, String password) {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                );

        authenticationManager.authenticate(authentication);

        return true;
    }
}
