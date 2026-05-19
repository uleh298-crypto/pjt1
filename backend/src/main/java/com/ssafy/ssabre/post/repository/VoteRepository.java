package com.ssafy.ssabre.post.repository;

import com.ssafy.ssabre.post.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    @Query("SELECT v FROM Vote v LEFT JOIN FETCH v.post WHERE v.post.id = :postId AND v.deletedAt IS NULL")
    Optional<Vote> findByPostIdAndDeletedAtIsNull(@Param("postId") Long postId);
}
