package com.ssafy.ssabre.project.repository;

import com.ssafy.ssabre.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.portfolio WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Project> findByDeletedAtIsNullOrderByCreatedAtDesc();

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.portfolio WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Project> findByIdAndDeletedAtIsNull(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.portfolio WHERE p.portfolio.id = :portfolioId AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Project> findByPortfolio_IdAndDeletedAtIsNullOrderByCreatedAtDesc(@Param("portfolioId") Long portfolioId);

    /**
     * ElementCollection(techStacks, urls, imageUrls)을 함께 로딩하는 쿼리
     * 포트폴리오별 프로젝트 조회 시 사용
     */
    @Query("SELECT DISTINCT p FROM Project p " +
           "LEFT JOIN FETCH p.portfolio " +
           "WHERE p.portfolio.id = :portfolioId AND p.deletedAt IS NULL " +
           "ORDER BY p.createdAt DESC")
    List<Project> findByPortfolioIdWithCollections(@Param("portfolioId") Long portfolioId);

    void deleteByPortfolioId(Long portfolioId);
}
