package com.ssafy.ssabre.portfolio.repository;

import com.ssafy.ssabre.portfolio.entity.PortfolioStack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PortfolioStackRepository extends JpaRepository<PortfolioStack, Long> {
    /**
     * 포트폴리오별 스택 조회 (stack fetch join으로 LAZY 로딩 문제 방지)
     */
    @Query("SELECT ps FROM PortfolioStack ps LEFT JOIN FETCH ps.stack WHERE ps.portfolio.id = :portfolioId AND ps.deletedAt IS NULL")
    List<PortfolioStack> findAllByPortfolioId(@Param("portfolioId") Long portfolioId);

    void deleteByPortfolioId(Long portfolioId);
}
