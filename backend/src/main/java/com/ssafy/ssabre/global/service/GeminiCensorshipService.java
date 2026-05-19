package com.ssafy.ssabre.global.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiCensorshipService implements AiCensorshipService {

    @Value("${ai.gemini.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String baseUrl;

    @Value("${ai.gemini.model:gemini-1.5-flash}")
    private String modelName;

    @Value("${ai.gemini.auth-key:}")
    private String apiKey;

    // With JacksonConfig in place, this will now be correctly injected by Spring
    private final ObjectMapper objectMapper;
    private RestClient restClient;

    private static final String PROMPT_TEMPLATE = """
                Classify the following text as 'safe' or 'unsafe' with a HIGH THRESHOLD for 'unsafe'.

                    'unsafe' ONLY includes:
                    - Target-directed insults or hate speech (attacking a person or group).
                    - Hardcore explicit profanity used with malicious intent.
                    - Explicit sexual content.

                    'safe' includes (DO NOT BLOCK):
                    - Slang or mild profanity used as emphasis or exclamation (e.g., "존나 맛있네", "미친 듯이 좋다").
                    - Words that are parts of clean words (e.g., "시발점", "변신").
                    - Informal talk between friends that isn't attacking anyone.
                    - If you are unsure, default to 'safe'.

                    Respond ONLY with a JSON object: {"safe": boolean, "reason": "short explanation in Korean"}

                    Text: "%s"
            """;

    @PostConstruct
    public void init() {
        this.baseUrl = adjustBaseUrl(this.baseUrl);
        log.info("GeminiCensorshipService initialized. Final URL: {}, Model: {}", baseUrl, modelName);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    private String adjustBaseUrl(String currentUrl) {
        if (currentUrl != null && currentUrl.endsWith("/gmsapi")) {
            log.info("Detected SSAFY Proxy URL (ending with /gmsapi). Appending upstream path.");
            return currentUrl + "/generativelanguage.googleapis.com/v1beta/models";
        }
        return currentUrl;
    }

    @Override
    public boolean isContentSafe(String content) {
        long startTime = System.currentTimeMillis();
        // Log entry immediately to confirm method call
        log.info("GeminiCensorshipService.isContentSafe called. Content length: {}",
                (content != null ? content.length() : "null"));

        try {
            if (isApiKeyInvalid()) {
                log.error(
                        "Gemini API Key is missing or invalid. Key value: '{}'. Skipping censorship (Returning Safe).",
                        apiKey);
                return true;
            }

            if (content == null || content.trim().isEmpty()) {
                log.debug("Content is empty. Returning safe.");
                return true;
            }

            String response = callGeminiApi(content);
            boolean isSafe = parseSafetyResult(response);

            return isSafe;

        } catch (Exception e) {
            log.error("Gemini censorship failed due to exception. Defaulting to safe.", e);
            return true;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("Gemini censorship execution attempt finished in {} ms.", duration);
        }
    }

    private boolean isApiKeyInvalid() {
        return apiKey == null || apiKey.isBlank() || "insert-your-key-here".equals(apiKey);
    }

    private String callGeminiApi(String content) {
        Map<String, Object> requestBody = createRequestBody(content);
        try {
            log.info("Sending request to Gemini API. Model: {}", modelName);
            String url = baseUrl + "/" + modelName + ":generateContent";
            log.info("Request URL: {}", url);
            if (apiKey != null && apiKey.length() > 10) {
                log.info("API Key injected: Length={}, Prefix={}, Suffix={}",
                        apiKey.length(),
                        apiKey.substring(0, 5),
                        apiKey.substring(apiKey.length() - 5));
            } else {
                log.warn("API Key is null or too short!");
            }

            if (log.isDebugEnabled()) {
                log.debug("Gemini Request Body: {}", objectMapper.writeValueAsString(requestBody));
            }

            String response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/" + modelName + ":generateContent")
                            .build())
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            log.info("Gemini API response received.");
            if (log.isDebugEnabled()) {
                log.debug("Gemini Raw Response: {}", response);
            }
            return response;
        } catch (Exception e) {
            log.error("Error occurred while calling Gemini API", e);
            throw new RuntimeException("Gemini API Call Failed", e);
        }
    }

    private Map<String, Object> createRequestBody(String content) {
        String escapedContent = content.replace("\\", "\\\\").replace("\"", "\\\"");
        String prompt = String.format(PROMPT_TEMPLATE, escapedContent);

        // Log the actual prompt being sent (content truncated if too long)
        String logPrompt = prompt.length() > 100 ? prompt.substring(0, 100) + "..." : prompt;
        log.debug("Generated Prompt: {}", logPrompt);

        return Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "response_mime_type", "application/json"));
    }

    private boolean parseSafetyResult(String response) {
        if (response == null) {
            log.warn("Gemini response is null.");
            return true;
        }

        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode candidates = root.path("candidates");
            if (candidates.isMissingNode() || candidates.isEmpty()) {
                log.warn("Gemini response contained no candidates: {}", response);
                return true;
            }

            String responseText = candidates.get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asString();

            if (responseText == null || responseText.isBlank()) {
                // 응답이 없으면 안전하다고 가정
                log.warn("Gemini candidate content text is empty.");
                return true;
            }

            // The model returns a JSON string inside the text field
            JsonNode resultNode = objectMapper.readTree(responseText);
            boolean isSafe = resultNode.path("safe").asBoolean(true);
            log.info("Gemini Analysis Result - Safe: {}", isSafe);
            return isSafe;
        } catch (JacksonException e) {
            log.error("Failed to parse inner JSON from Gemini response: {}", response, e);
            return true; // Parsing error -> default to safe
        }
    }
}
