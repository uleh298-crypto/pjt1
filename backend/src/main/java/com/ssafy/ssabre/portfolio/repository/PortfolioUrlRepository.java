package com.ssafy.ssabre.portfolio.repository;

import com.ssafy.ssabre.portfolio.entity.PortfolioUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PortfolioUrlRepository extends JpaRepository<PortfolioUrl, Long> {
    List<PortfolioUrl> findAllByPortfolioId(Long portfolioId);

    void deleteByPortfolioId(Long portfolioId);
}
