package com.ssafy.ssabre.report.repository;

import com.ssafy.ssabre.report.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("SELECT r FROM Report r WHERE r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    Page<Report> findAllNotDeleted(Pageable pageable);

    void deleteByReporterId(Long reporterId);
}
