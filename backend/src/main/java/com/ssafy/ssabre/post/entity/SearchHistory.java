package com.ssafy.ssabre.post.entity;

import com.ssafy.ssabre.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "search_history")
public class SearchHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, length = 255)
    private String keyword;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public SearchHistory(Long memberId, String keyword) {
        this.memberId = memberId;
        this.keyword = keyword;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
