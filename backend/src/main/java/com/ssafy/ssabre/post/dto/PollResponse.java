package com.ssafy.ssabre.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "투표 응답")
public record PollResponse(
        @Schema(description = "투표 ID", example = "77")
        Long pollId,

        @Schema(description = "총 투표 수", example = "5")
        Integer totalVotes,

        @Schema(description = "내가 투표한 옵션 ID", example = "2")
        Long myVotedOptionId,

        @Schema(description = "투표 옵션 목록")
        List<PollOptionResponse> options
) {
}
