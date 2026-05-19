package com.ssafy.ssabre.global.service;

public interface AiCensorshipService {
    /**
     * Checks if the content is safe.
     * @param content content to check
     * @return true if safe, false if inappropriate
     */
    boolean isContentSafe(String content);
}
