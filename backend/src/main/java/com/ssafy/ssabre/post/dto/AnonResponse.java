package com.ssafy.ssabre.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "익명 정보")
public record AnonResponse(
        @Schema(description = "익명 이름", example = "싸용자1")
        String name,

        @Schema(description = "게시글 작성자 여부")
        Boolean isAuthor,

        @Schema(description = "본인 여부")
        Boolean isMine
) {
}
