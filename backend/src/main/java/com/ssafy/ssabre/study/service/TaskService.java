package com.ssafy.ssabre.study.service;

import com.ssafy.ssabre.global.entity.TaskStatus;
import com.ssafy.ssabre.global.error.GlobalErrorCode;
import com.ssafy.ssabre.global.error.exception.BusinessException;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.MemberRepository;
import com.ssafy.ssabre.study.dto.TaskResponse;
import com.ssafy.ssabre.study.entity.Study;
import com.ssafy.ssabre.study.entity.StudyTask;
import com.ssafy.ssabre.study.repository.StudyMemberRepository;
import com.ssafy.ssabre.study.repository.StudyRepository;
import com.ssafy.ssabre.study.repository.StudyTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service("studyTaskService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final StudyTaskRepository taskRepository;
    private final StudyRepository studyRepository;
    private final MemberRepository memberRepository;
    private final StudyMemberRepository studyMemberRepository;

    public List<TaskResponse> getTasks(Long studyId) {
        return taskRepository.findByStudyIdOrderByStartDateAsc(studyId).stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse createTask(String email, Long studyId, String title, String content, LocalDate startDate,
            LocalDate endDate, TaskStatus status) {
        Member member = getMemberByEmail(email);
        Study study = getStudyById(studyId);

        validateStudyMember(member, study);

        StudyTask savedTask = taskRepository.save(StudyTask.create(study, member, title, content, startDate, endDate, status));
        return TaskResponse.from(savedTask);
    }

    @Transactional
    public void updateTask(String email, Long taskId, String title, String content, LocalDate startDate,
            LocalDate endDate, TaskStatus status) {
        Member member = getMemberByEmail(email);
        StudyTask task = getTaskById(taskId);

        validateStudyMember(member, task.getStudy());

        task.update(title, content, startDate, endDate, status);
    }

    @Transactional
    public void updateTaskStatus(String email, Long taskId, TaskStatus status) {
        StudyTask task = getTaskById(taskId);
        task.updateStatus(status);
    }

    @Transactional
    public void deleteTask(String email, Long taskId) {
        StudyTask task = getTaskById(taskId);
        taskRepository.delete(task);
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.MEMBER_NOT_FOUND));
    }

    private Study getStudyById(Long studyId) {
        return studyRepository.findByIdAndNotDeleted(studyId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.STUDY_NOT_FOUND));
    }

    private StudyTask getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.ENTITY_NOT_FOUND));
    }

    private void validateStudyMember(Member member, Study study) {
        boolean isLeader = study.getLeader() != null && study.getLeader().getId().equals(member.getId());
        boolean isActiveMember = studyMemberRepository.existsActiveMember(study, member);
        if (!isLeader && !isActiveMember) {
            throw new BusinessException(GlobalErrorCode.NOT_MEMBER);
        }
    }
}
