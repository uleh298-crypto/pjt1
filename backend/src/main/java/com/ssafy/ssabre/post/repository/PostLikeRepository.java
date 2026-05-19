package com.ssafy.ssabre.post.repository;

import com.ssafy.ssabre.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLike.PostLikeId> {
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);
    void deleteByPostIdAndMemberId(Long postId, Long memberId);
    void deleteByMemberId(Long memberId);
}
