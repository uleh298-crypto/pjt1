package com.ssafy.ssabre.notification.scheduler;

import com.ssafy.ssabre.notification.service.FCMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final FCMService fcmService;
    private static final String TOPIC = "daily_reminder";

    /**
     * 매일 아침 8시 50분에 입실 알림 전송 (평일만)
     */
    @Scheduled(cron = "0 50 8 * * MON-FRI", zone = "Asia/Seoul")
    public void sendMorningEntryReminder() {
        log.info("Sending morning entry reminder to topic: {}", TOPIC);
        fcmService.sendToTopic(
                TOPIC,
                "🌅 출근 시간입니다!",
                "오늘도 좋은 하루 되세요. 입실 체크를 잊지 마세요!");
    }

    /**
     * 매일 저녁 6시에 퇴실 알림 전송 (평일만)
     */
    @Scheduled(cron = "0 0 18 * * MON-FRI", zone = "Asia/Seoul")
    public void sendEveningExitReminder() {
        log.info("Sending evening exit reminder to topic: {}", TOPIC);
        fcmService.sendToTopic(
                TOPIC,
                "🌙 퇴근 시간입니다!",
                "오늘 하루도 수고하셨습니다. 퇴실 체크를 잊지 마세요!");
    }
}
