package com.ssafy.ssabre.post.repository;

import com.ssafy.ssabre.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.board LEFT JOIN FETCH p.member WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Post> findByDeletedAtIsNullOrderByCreatedAtDesc();

    long countByMember_IdAndDeletedAtIsNull(Long memberId);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.board LEFT JOIN FETCH p.member WHERE p.board.id = :boardId AND p.deletedAt IS NULL AND p.isBlinded = false ORDER BY p.createdAt DESC LIMIT 1")
    Optional<Post> findLatestVisiblePost(@Param("boardId") Long boardId);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.board LEFT JOIN FETCH p.member WHERE p.board.id = :boardId AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Post> findByBoardIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long boardId);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.board LEFT JOIN FETCH p.member WHERE p.member.id = :memberId AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Post> findByMember_IdAndDeletedAtIsNullOrderByCreatedAtDesc(@Param("memberId") Long memberId);

    @Query("SELECT p FROM Post p WHERE p.member.id = :memberId")
    List<Post> findAllByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.board LEFT JOIN FETCH p.member WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Post> findByIdAndDeletedAtIsNull(@Param("id") Long id);

    boolean existsByIdAndDeletedAtIsNull(Long id);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.board LEFT JOIN FETCH p.member WHERE p.deletedAt IS NULL AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY p.createdAt DESC")
    List<Post> searchByKeyword(@Param("keyword") String keyword);

    // 커서 기반 페이지네이션 - 전체 게시글
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.board LEFT JOIN FETCH p.member WHERE p.deletedAt IS NULL " +
           "AND (p.createdAt < :cursorTime OR (p.createdAt = :cursorTime AND p.id < :cursorId)) " +
           "ORDER BY p.createdAt DESC, p.id DESC")
    List<Post> findAllWithCursor(@Param("cursorTime") LocalDateTime cursorTime,
                                 @Param("cursorId") Long cursorId,
                                 Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.board LEFT JOIN FETCH p.member WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC, p.id DESC")
    List<Post> findAllFirstPage(Pageable pageable);

    // 커서 기반 페이지네이션 - 게시판별
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.board LEFT JOIN FETCH p.member WHERE p.deletedAt IS NULL AND p.board.id = :boardId " +
           "AND (p.createdAt < :cursorTime OR (p.createdAt = :cursorTime AND p.id < :cursorId)) " +
           "ORDER BY p.createdAt DESC, p.id DESC")
    List<Post> findByBoardIdWithCursor(@Param("boardId") Long boardId,
                                       @Param("cursorTime") LocalDateTime cursorTime,
                                       @Param("cursorId") Long cursorId,
                                       Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.board LEFT JOIN FETCH p.member WHERE p.deletedAt IS NULL AND p.board.id = :boardId " +
           "ORDER BY p.createdAt DESC, p.id DESC")
    List<Post> findByBoardIdFirstPage(@Param("boardId") Long boardId, Pageable pageable);

    // 커서 기반 페이지네이션 - 검색
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.board LEFT JOIN FETCH p.member WHERE p.deletedAt IS NULL " +
           "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (p.createdAt < :cursorTime OR (p.createdAt = :cursorTime AND p.id < :cursorId)) " +
           "ORDER BY p.createdAt DESC, p.id DESC")
    List<Post> searchByKeywordWithCursor(@Param("keyword") String keyword,
                                         @Param("cursorTime") LocalDateTime cursorTime,
                                         @Param("cursorId") Long cursorId,
                                         Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.board LEFT JOIN FETCH p.member WHERE p.deletedAt IS NULL " +
           "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.createdAt DESC, p.id DESC")
    List<Post> searchByKeywordFirstPage(@Param("keyword") String keyword, Pageable pageable);

    // Hot 게시글 (좋아요 10개 이상) - 커서 기반 페이지네이션
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.board LEFT JOIN FETCH p.member WHERE p.deletedAt IS NULL AND p.likeCount >= :minLikeCount " +
           "AND (p.createdAt < :cursorTime OR (p.createdAt = :cursorTime AND p.id < :cursorId)) " +
           "ORDER BY p.createdAt DESC, p.id DESC")
    List<Post> findHotPostsWithCursor(@Param("minLikeCount") int minLikeCount,
                                      @Param("cursorTime") LocalDateTime cursorTime,
                                      @Param("cursorId") Long cursorId,
                                      Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.board LEFT JOIN FETCH p.member WHERE p.deletedAt IS NULL AND p.likeCount >= :minLikeCount " +
           "ORDER BY p.createdAt DESC, p.id DESC")
    List<Post> findHotPostsFirstPage(@Param("minLikeCount") int minLikeCount, Pageable pageable);
}
