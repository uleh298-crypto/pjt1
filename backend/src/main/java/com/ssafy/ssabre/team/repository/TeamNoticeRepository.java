package com.ssafy.ssabre.team.repository;

import com.ssafy.ssabre.team.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamNoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findByTeamIdOrderByIsPinnedDescCreatedAtDesc(Long teamId);
}
