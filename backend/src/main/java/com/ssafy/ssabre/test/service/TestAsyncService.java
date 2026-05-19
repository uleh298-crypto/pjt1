package com.ssafy.ssabre.test.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestAsyncService {

    @Async
    public void runAsyncJob() {
        log.info("[Async Test] Start - This should valid be non-blocking.");
        try {
            Thread.sleep(3000); // 3 seconds delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("[Async Test] End - If you see this 3 seconds later, Async is working!");
    }
}
