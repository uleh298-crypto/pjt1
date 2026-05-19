package com.ssafy.ssabre.comment.repository;

import com.ssafy.ssabre.comment.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.post LEFT JOIN FETCH c.member WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<Comment> findByIdAndDeletedAtIsNull(@Param("id") Long id);

    long countByMember_IdAndDeletedAtIsNull(Long memberId);

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.post LEFT JOIN FETCH c.member WHERE c.post.id = :postId AND c.parentId IS NULL AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findByPostIdAndParentIdIsNullAndDeletedAtIsNullOrderByCreatedAtAsc(@Param("postId") Long postId);

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.post LEFT JOIN FETCH c.member WHERE c.parentId = :parentId AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findByParentIdAndDeletedAtIsNullOrderByCreatedAtAsc(@Param("parentId") Long parentId);

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.post p LEFT JOIN FETCH p.board LEFT JOIN FETCH c.member WHERE c.member.id = :memberId AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findByMember_IdAndDeletedAtIsNullOrderByCreatedAtDesc(@Param("memberId") Long memberId);

    @Query("SELECT c FROM Comment c WHERE c.member.id = :memberId")
    List<Comment> findAllByMemberId(@Param("memberId") Long memberId);

    // 삭제된 댓글도 포함하여 조회 (soft-delete 지원)
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.post LEFT JOIN FETCH c.member WHERE c.post.id = :postId AND c.parentId IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findByPostIdAndParentIdIsNullOrderByCreatedAtAsc(@Param("postId") Long postId);

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.post LEFT JOIN FETCH c.member WHERE c.parentId = :parentId ORDER BY c.createdAt ASC")
    List<Comment> findByParentIdOrderByCreatedAtAsc(@Param("parentId") Long parentId);

    // 커서 기반 페이지네이션 - 부모 댓글 (첫 페이지)
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.post LEFT JOIN FETCH c.member WHERE c.post.id = :postId AND c.parentId IS NULL " +
           "ORDER BY c.createdAt ASC, c.id ASC")
    List<Comment> findParentCommentsFirstPage(@Param("postId") Long postId, Pageable pageable);

    // 커서 기반 페이지네이션 - 부모 댓글 (커서 이후)
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.post LEFT JOIN FETCH c.member WHERE c.post.id = :postId AND c.parentId IS NULL " +
           "AND (c.createdAt > :cursorTime OR (c.createdAt = :cursorTime AND c.id > :cursorId)) " +
           "ORDER BY c.createdAt ASC, c.id ASC")
    List<Comment> findParentCommentsWithCursor(@Param("postId") Long postId,
                                               @Param("cursorTime") LocalDateTime cursorTime,
                                               @Param("cursorId") Long cursorId,
                                               Pageable pageable);
}
