package com.ssafy.ssabre.notification.service;

import com.google.firebase.messaging.*;
import com.ssafy.ssabre.notification.dto.FCMDataType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FCMService {

    private final FirebaseMessaging firebaseMessaging;

    @Async
    public void sendNotification(String token, String title, String body) {
        if (token == null || token.isEmpty()) {
            return;
        }

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .build();

        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM message", e);
        }
    }

    /**
     * FCM Data 메시지 발송 (백그라운드 수신용)
     * iOS: ApnsConfig로 알림 배너 표시 + data 전달
     * Android: data만 전송하여 클라이언트가 직접 처리
     */
    @Async
    public void sendDataMessage(String token, FCMDataType type, Map<String, String> data) {
        if (token == null || token.isEmpty()) {
            log.debug("FCM token is null or empty, skipping data message");
            return;
        }

        // type을 data에 포함
        data.put("type", type.name());

        // iOS 푸시 알림을 위한 ApnsConfig 설정
        ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setAlert(ApsAlert.builder()
                                .setTitle(getNotificationTitle(type, data))
                                .setBody(getNotificationBody(type, data))
                                .build())
                        .setSound("default")
                        .build())
                .build();

        Message message = Message.builder()
                .setToken(token)
                .putAllData(data)
                .setApnsConfig(apnsConfig) // iOS 알림 배너 표시
                .build();

        try {
            String messageId = firebaseMessaging.send(message);
            log.debug("FCM data message sent successfully: messageId={}, type={}", messageId, type);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM data message: type={}, error={}", type, e.getMessage());
        }
    }

    /**
     * 특정 토큰을 Topic에 구독시킵니다.
     * 
     * @param token FCM 토큰
     * @param topic Topic 이름
     */
    public void subscribeToTopic(String token, String topic) {
        try {
            com.google.firebase.messaging.TopicManagementResponse response = firebaseMessaging
                    .subscribeToTopic(java.util.List.of(token), topic);
            log.info("Successfully subscribed to topic '{}': {} success, {} failures",
                    topic, response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("Failed to subscribe to topic '{}'", topic, e);
            throw new RuntimeException("Failed to subscribe to topic: " + topic, e);
        }
    }

    /**
     * 특정 토큰을 Topic에서 구독 해지합니다.
     * 
     * @param token FCM 토큰
     * @param topic Topic 이름
     */
    public void unsubscribeFromTopic(String token, String topic) {
        try {
            com.google.firebase.messaging.TopicManagementResponse response = firebaseMessaging
                    .unsubscribeFromTopic(java.util.List.of(token), topic);
            log.info("Successfully unsubscribed from topic '{}': {} success, {} failures",
                    topic, response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("Failed to unsubscribe from topic '{}'", topic, e);
            throw new RuntimeException("Failed to unsubscribe from topic: " + topic, e);
        }
    }

    /**
     * FCM 알림 타입별 제목 생성
     */
    private String getNotificationTitle(FCMDataType type, Map<String, String> data) {
        return switch (type) {
            case CHAT_NEW -> "새 메시지";
            case POST_COMMENT -> "새 댓글";
            case COMMENT_REPLY -> "새 답글";
            case APPLICATION_NEW -> "새 지원";
            case APPLICATION_ACCEPTED -> "지원 승인";
            case APPLICATION_REJECTED -> "지원 거절";
        };
    }

    /**
     * FCM 알림 타입별 내용 생성
     */
    private String getNotificationBody(FCMDataType type, Map<String, String> data) {
        return switch (type) {
            case CHAT_NEW -> {
                String senderName = data.get("senderName");
                String preview = data.get("preview");
                if (senderName != null && preview != null) {
                    yield senderName + ": " + preview;
                } else if (preview != null) {
                    yield preview;
                } else {
                    yield "새로운 메시지가 도착했습니다";
                }
            }
            case POST_COMMENT -> "게시글에 댓글이 달렸습니다";
            case COMMENT_REPLY -> "댓글에 답글이 달렸습니다";
            case APPLICATION_NEW -> {
                String groupTitle = data.get("groupTitle");
                if (groupTitle != null) {
                    yield groupTitle + "에 새로운 지원이 있습니다";
                } else {
                    yield "새로운 지원이 있습니다";
                }
            }
            case APPLICATION_ACCEPTED -> {
                String groupTitle = data.get("groupTitle");
                if (groupTitle != null) {
                    yield groupTitle + " 지원이 승인되었습니다";
                } else {
                    yield "지원이 승인되었습니다";
                }
            }
            case APPLICATION_REJECTED -> {
                String groupTitle = data.get("groupTitle");
                if (groupTitle != null) {
                    yield groupTitle + " 지원이 거절되었습니다";
                } else {
                    yield "지원이 거절되었습니다";
                }
            }
        };
    }

    /**
     * Topic에 메시지를 발송합니다.
     * 
     * @param topic Topic 이름
     * @param title 알림 제목
     * @param body  알림 내용
     */
    public void sendToTopic(String topic, String title, String body) {
        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setTopic(topic)
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("Successfully sent message to topic '{}': {}", topic, response);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send message to topic '{}'", topic, e);
            throw new RuntimeException("Failed to send message to topic: " + topic, e);
        }
    }
}
