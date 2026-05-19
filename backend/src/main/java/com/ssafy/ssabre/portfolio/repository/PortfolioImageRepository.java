package com.ssafy.ssabre.portfolio.repository;

import com.ssafy.ssabre.portfolio.entity.PortfolioImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PortfolioImageRepository extends JpaRepository<PortfolioImage, Long> {
    List<PortfolioImage> findAllByPortfolioId(Long portfolioId);

    void deleteByPortfolioId(Long portfolioId);
}
