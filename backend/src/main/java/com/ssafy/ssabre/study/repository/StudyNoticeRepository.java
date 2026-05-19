package com.ssafy.ssabre.study.repository;

import com.ssafy.ssabre.study.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyNoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findByStudyIdOrderByIsPinnedDescCreatedAtDesc(Long studyId);
}
