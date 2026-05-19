package com.ssafy.ssabre.auth.exception;

import tools.jackson.databind.ObjectMapper;
import com.ssafy.ssabre.global.error.ErrorResponse;
import com.ssafy.ssabre.global.error.GlobalErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        log.error("Unauthorized error: {}", authException.getMessage());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Access Denied / Unauthorized logic
        // If necessary, we can differentiate errors based on the exception message or
        // type
        final ErrorResponse errorResponse = ErrorResponse.of(GlobalErrorCode.ACCESS_DENIED, authException.getMessage());

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
