package com.ssafy.ssabre.campus.entity;

import com.ssafy.ssabre.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "classes")
public class Classes extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "반 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id", nullable = false)
    private Campus campus;

    @Column(nullable = false, length = 50)
    @Schema(description = "반 이름", example = "1반")
    private String name;

    @Column(nullable = false)
    @Schema(description = "기수", example = "10")
    private Integer generation;

    @Column(name = "class_no", nullable = false)
    @Schema(description = "반 번호", example = "1")
    private Integer classNo;

    @Column(name = "track_type", length = 20)
    @Schema(description = "트랙", example = "JAVA_BACKEND")
    private String trackType;

    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;

    @Builder
    public Classes(Campus campus, String name, Integer generation, Integer classNo, String trackType) {
        this.campus = campus;
        this.name = name;
        this.generation = generation;
        this.classNo = classNo;
        this.trackType = trackType;
    }

    public void update(Campus campus, String name, Integer generation, Integer classNo, String trackType) {
        this.campus = campus;
        this.name = name;
        this.generation = generation;
        this.classNo = classNo;
        this.trackType = trackType;
    }

    public void delete() {
        this.deletedAt = java.time.LocalDateTime.now();
    }
}
