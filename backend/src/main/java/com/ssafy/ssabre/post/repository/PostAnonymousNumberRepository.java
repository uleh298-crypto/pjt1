package com.ssafy.ssabre.post.repository;

import com.ssafy.ssabre.post.entity.PostAnonymousNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostAnonymousNumberRepository extends JpaRepository<PostAnonymousNumber, Long> {

    /**
     * 게시글에서 해당 사용자의 익명 번호 조회
     */
    Optional<PostAnonymousNumber> findByPostIdAndMemberId(Long postId, Long memberId);

    /**
     * 게시글에서 현재 최대 익명 번호 조회
     */
    @Query("SELECT COALESCE(MAX(p.anonymousNumber), 0) FROM PostAnonymousNumber p WHERE p.post.id = :postId")
    Integer findMaxAnonymousNumberByPostId(@Param("postId") Long postId);

    void deleteByMemberId(Long memberId);
}
