package com.ssafy.ssabre.post.repository;

import com.ssafy.ssabre.post.entity.SearchHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    List<SearchHistory> findByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long memberId);

    Optional<SearchHistory> findByIdAndMemberIdAndDeletedAtIsNull(Long id, Long memberId);

    Optional<SearchHistory> findByMemberIdAndKeywordAndDeletedAtIsNull(Long memberId, String keyword);

    // 인기 검색어 TOP 10 (keyword별 검색 횟수)
    @Query("SELECT s.keyword, COUNT(s) as cnt FROM SearchHistory s WHERE s.deletedAt IS NULL GROUP BY s.keyword ORDER BY cnt DESC")
    List<Object[]> findPopularKeywords(Pageable pageable);

    // 사용자별 검색 기록 개수
    long countByMemberIdAndDeletedAtIsNull(Long memberId);

    // 사용자별 오래된 검색 기록 조회 (limit 초과분 삭제용)
    @Query("SELECT s FROM SearchHistory s WHERE s.memberId = :memberId AND s.deletedAt IS NULL ORDER BY s.createdAt ASC")
    List<SearchHistory> findOldestByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    // 사용자별 검색 기록 전체 삭제 (soft delete)
    @Modifying
    @Query("UPDATE SearchHistory s SET s.deletedAt = CURRENT_TIMESTAMP WHERE s.memberId = :memberId AND s.deletedAt IS NULL")
    void deleteAllByMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("DELETE FROM SearchHistory s WHERE s.memberId = :memberId")
    void hardDeleteAllByMemberId(@Param("memberId") Long memberId);
}
