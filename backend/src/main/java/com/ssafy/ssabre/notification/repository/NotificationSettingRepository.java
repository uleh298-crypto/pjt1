package com.ssafy.ssabre.notification.repository;

import com.ssafy.ssabre.notification.entity.NotificationSetting;
import com.ssafy.ssabre.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
    Optional<NotificationSetting> findByMemberIdAndNotificationType(Long memberId, NotificationType notificationType);

    void deleteByMemberId(Long memberId);
}
