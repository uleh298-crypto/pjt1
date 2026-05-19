package com.ssafy.ssabre.portfolio.repository;

import com.ssafy.ssabre.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    @Query("SELECT p FROM Portfolio p LEFT JOIN FETCH p.member WHERE p.member.id = :memberId")
    List<Portfolio> findByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT p FROM Portfolio p LEFT JOIN FETCH p.member WHERE p.id = :id")
    Optional<Portfolio> findByIdWithMember(@Param("id") Long id);

    /**
     * 여러 멤버의 포트폴리오를 한 번에 조회 (N+1 방지)
     */
    @Query("SELECT p FROM Portfolio p WHERE p.member.id IN :memberIds")
    List<Portfolio> findByMemberIdIn(@Param("memberIds") List<Long> memberIds);

    /**
     * 모든 포트폴리오 조회 (member fetch join)
     */
    @Query("SELECT p FROM Portfolio p LEFT JOIN FETCH p.member")
    List<Portfolio> findAllWithMember();
}
