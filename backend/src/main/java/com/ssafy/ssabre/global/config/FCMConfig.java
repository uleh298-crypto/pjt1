package com.ssafy.ssabre.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Configuration
public class FCMConfig {

    @Bean
    FirebaseMessaging firebaseMessaging() throws IOException {
        ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
        InputStream refreshToken = resource.getInputStream();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(refreshToken))
                .build();

        FirebaseApp app = null;
        List<FirebaseApp> apps = FirebaseApp.getApps();
        if (apps != null && !apps.isEmpty()) {
            for (FirebaseApp a : apps) {
                if (a.getName().equals(FirebaseApp.DEFAULT_APP_NAME)) {
                    app = a;
                }
            }
        }

        if (app == null) {
            app = FirebaseApp.initializeApp(options);
        }

        return FirebaseMessaging.getInstance(app);
    }
}
