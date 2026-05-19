package com.ssafy.ssabre.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "post_images")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "게시글 이미지 정보")
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "이미지 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @Schema(description = "게시글 정보")
    private Post post;

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
