package com.ssafy.ssabre.auth.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class StudentMattermostLoader {

    private final Set<String> studentDataSet = new HashSet<>();

    @PostConstruct
    public void loadCsvData() {
        try {
            ClassPathResource resource = new ClassPathResource("ssafy_members_data.csv");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                int count = 0;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        String generation = parts[0].trim();
                        String name = parts[1].trim();
                        String mattermostId = parts[2].trim();

                        // "기수:이름:mattermostId" 형태로 저장
                        String key = generation + ":" + name + ":" + mattermostId;
                        studentDataSet.add(key);
                        count++;
                    }
                }
                log.info("CSV 데이터 로드 완료: {}건", count);
            }
        } catch (Exception e) {
            log.error("CSV 파일 로드 실패", e);
        }
    }

    public boolean validate(Integer generation, String name, String mattermostId) {
        String key = generation + ":" + name + ":" + mattermostId;
        return studentDataSet.contains(key);
    }

    public int getDataCount() {
        return studentDataSet.size();
    }
}
