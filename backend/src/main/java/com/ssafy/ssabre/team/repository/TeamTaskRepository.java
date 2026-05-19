package com.ssafy.ssabre.team.repository;

import com.ssafy.ssabre.team.entity.TeamTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamTaskRepository extends JpaRepository<TeamTask, Long> {
    List<TeamTask> findByTeamIdOrderByStartDateAsc(Long teamId);

    List<TeamTask> findByCreatorId(Long creatorId);
}
