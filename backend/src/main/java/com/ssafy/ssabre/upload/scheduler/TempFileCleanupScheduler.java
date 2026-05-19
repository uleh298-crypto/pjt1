package com.ssafy.ssabre.upload.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class TempFileCleanupScheduler {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 매일 새벽 3시에 24시간 이상 지난 temp 파일 삭제
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupTempFiles() {
        Path tempPath = Paths.get(uploadDir, "temp");

        if (!Files.exists(tempPath)) {
            return;
        }

        Instant cutoffTime = Instant.now().minus(24, ChronoUnit.HOURS);
        int deletedCount = 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempPath)) {
            for (Path file : stream) {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
                    if (attrs.creationTime().toInstant().isBefore(cutoffTime)) {
                        Files.delete(file);
                        deletedCount++;
                    }
                } catch (IOException e) {
                    log.warn("Failed to delete temp file: {}", file, e);
                }
            }
        } catch (IOException e) {
            log.error("Failed to cleanup temp files", e);
        }

        if (deletedCount > 0) {
            log.info("Cleaned up {} temp files", deletedCount);
        }
    }
}
