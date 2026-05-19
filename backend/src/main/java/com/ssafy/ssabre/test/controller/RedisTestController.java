package com.ssafy.ssabre.test.controller;

import com.ssafy.ssabre.test.service.TestAsyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class RedisTestController {

    private final StringRedisTemplate redisTemplate;
    private final TestAsyncService testAsyncService;

    @GetMapping("/redis/keys")
    public Set<String> getAllKeys() {
        return redisTemplate.keys("*");
    }

    @GetMapping("/redis/get")
    public Object getValue(@RequestParam String key) {
        String value = redisTemplate.opsForValue().get(key);
        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("value", value); // Returns Raw JSON string
        // If it's cached binary or serialized with generic jackson, it might look like
        // JSON.
        return response;
    }

    @GetMapping("/async")
    public String testAsync() {
        testAsyncService.runAsyncJob();
        return "Async Job Started! Check server logs. (You should get this response immediately)";
    }
}
