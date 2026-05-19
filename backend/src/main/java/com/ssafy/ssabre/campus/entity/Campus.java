package com.ssafy.ssabre.campus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "campuses")
@Getter
@NoArgsConstructor
public class Campus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "캠퍼스 ID", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "캠퍼스 이름", example = "서울")
    private String name;

    public Campus(String name) {
        this.name = name;
    }
}
