package com.ssafy.ssabre.auth.service;

import com.ssafy.ssabre.auth.dto.LoginRequest;
import com.ssafy.ssabre.auth.jwt.JwtTokenProvider;
import com.ssafy.ssabre.auth.jwt.TokenInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenInfo login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                request.email(), request.password());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        return jwtTokenProvider.generateToken(authentication);
    }

    @Transactional
    public TokenInfo refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid Refresh Token");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);

        return jwtTokenProvider.generateToken(authentication);
    }
}
