package com.ssafy.ssabre.post.repository;

import com.ssafy.ssabre.post.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapRepository extends JpaRepository<Scrap, Scrap.ScrapId> {
    long countByMemberId(Long memberId);
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    @Query("SELECT s FROM Scrap s LEFT JOIN FETCH s.post p LEFT JOIN FETCH p.board LEFT JOIN FETCH p.member WHERE s.member.id = :memberId ORDER BY s.createdAt DESC")
    List<Scrap> findByMemberIdOrderByCreatedAtDesc(@Param("memberId") Long memberId);

    void deleteByPostIdAndMemberId(Long postId, Long memberId);

    void deleteByMemberId(Long memberId);
}
