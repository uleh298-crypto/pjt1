package com.ssafy.ssabre.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class AiCensorshipManager implements AiCensorshipService {

    private final GeminiCensorshipService geminiCensorshipService;

    @Override
    public boolean isContentSafe(String content) {
        return geminiCensorshipService.isContentSafe(content);
    }
}
