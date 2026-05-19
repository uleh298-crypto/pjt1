package com.ssafy.ssabre.campus.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "반 생성/수정 요청 DTO")
public class ClassesRequestDto {

    @Schema(description = "캠퍼스 ID", example = "1")
    private Long campusId;

    @Schema(description = "반 이름", example = "1반")
    private String name;

    @Schema(description = "기수", example = "10")
    private Integer generation;

    @Schema(description = "반 번호", example = "1")
    private Integer classNo;

    @Schema(description = "트랙", example = "JAVA_BACKEND")
    private String trackType;
}
