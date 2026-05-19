package com.ssafy.ssabre.study.repository;

import com.ssafy.ssabre.study.entity.StudyTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyTaskRepository extends JpaRepository<StudyTask, Long> {
    List<StudyTask> findByStudyIdOrderByStartDateAsc(Long studyId);

    List<StudyTask> findByCreatorId(Long creatorId);
}
