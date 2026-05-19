package com.ssafy.ssabre.portfolio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "stacks")
@Getter
@NoArgsConstructor
@Schema(description = "기술 스택 정보")
public class Stack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "스택 ID", example = "1")
    private Long id;

    @Schema(description = "스택 이름", example = "Java")
    private String name;

    @Column(name = "img_url", columnDefinition = "TEXT")
    @Schema(description = "스택 이미지 URL", example = "https://example.com/java.png")
    private String imgUrl;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
