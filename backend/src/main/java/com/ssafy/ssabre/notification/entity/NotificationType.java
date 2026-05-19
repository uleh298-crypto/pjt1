package com.ssafy.ssabre.notification.entity;

public enum NotificationType {
    COMMENT("새 댓글"),
    REPLY("새 답글"),
    MESSAGE("새 쪽지"),
    NOTICE("공지사항"),
    APPLICATION("지원 알림"),
    ETC("알림");

    private final String defaultMessage;

    NotificationType(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
