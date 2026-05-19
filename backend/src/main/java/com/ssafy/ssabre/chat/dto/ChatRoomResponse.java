package com.ssafy.ssabre.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "채팅방 응답")
public record ChatRoomResponse(
        @Schema(description = "채팅방 ID", example = "1")
        Long roomId,

        @Schema(description = "채팅방 표시 이름", example = "맛집 추천의 싸용자3")
        String chatRoomName,

        @Schema(description = "상대방 익명 이름", example = "싸용자3")
        String opponentName,

        @Schema(description = "연결된 게시글 ID")
        Long postId,

        @Schema(description = "연결된 게시글 제목")
        String postTitle,

        @Schema(description = "마지막 메시지 내용")
        String lastMessage,

        @Schema(description = "마지막 메시지 시간")
        LocalDateTime lastMessageAt,

        @Schema(description = "삭제 여부")
        boolean isDeleted,

        @Schema(description = "생성 시간")
        LocalDateTime createdAt
) {
}
