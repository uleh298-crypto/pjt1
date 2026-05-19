package com.ssafy.ssabre.comment.repository;

import com.ssafy.ssabre.comment.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLike.CommentLikeId> {
    boolean existsByCommentIdAndMemberId(Long commentId, Long memberId);

    void deleteByCommentIdAndMemberId(Long commentId, Long memberId);

    void deleteByMemberId(Long memberId);
}
