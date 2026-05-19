package com.ssafy.ssabre.campus.dto;

import com.ssafy.ssabre.campus.entity.Classes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "반 응답 DTO")
public class ClassesResponseDto {

    @Schema(description = "반 ID", example = "1")
    private Long id;

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

    public ClassesResponseDto(Classes classes) {
        this.id = classes.getId();
        this.campusId = classes.getCampus().getId();
        this.name = classes.getName();
        this.generation = classes.getGeneration();
        this.classNo = classes.getClassNo();
        this.trackType = classes.getTrackType();
    }
}
