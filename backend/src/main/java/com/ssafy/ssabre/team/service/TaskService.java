package com.ssafy.ssabre.team.service;

import com.ssafy.ssabre.global.entity.TaskStatus;
import com.ssafy.ssabre.global.error.GlobalErrorCode;
import com.ssafy.ssabre.global.error.exception.BusinessException;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.MemberRepository;
import com.ssafy.ssabre.team.dto.TaskResponse;
import com.ssafy.ssabre.team.entity.Team;
import com.ssafy.ssabre.team.entity.TeamTask;
import com.ssafy.ssabre.team.repository.TeamMemberRepository;
import com.ssafy.ssabre.team.repository.TeamRepository;
import com.ssafy.ssabre.team.repository.TeamTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service("teamTaskService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TeamTaskRepository taskRepository;
    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final TeamMemberRepository teamMemberRepository;

    public List<TaskResponse> getTasks(Long teamId) {
        return taskRepository.findByTeamIdOrderByStartDateAsc(teamId).stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse createTask(String email, Long teamId, String title, String content, LocalDate startDate,
            LocalDate endDate, TaskStatus status) {
        Member member = getMemberByEmail(email);
        Team team = getTeamById(teamId);

        validateTeamMember(member, team);

        TeamTask savedTask = taskRepository.save(TeamTask.create(team, member, title, content, startDate, endDate, status));
        return TaskResponse.from(savedTask);
    }

    @Transactional
    public void updateTask(String email, Long taskId, String title, String content, LocalDate startDate,
            LocalDate endDate, TaskStatus status) {
        Member member = getMemberByEmail(email);
        TeamTask task = getTaskById(taskId);

        validateTeamMember(member, task.getTeam());

        task.update(title, content, startDate, endDate, status);
    }

    @Transactional
    public void updateTaskStatus(String email, Long taskId, TaskStatus status) {
        TeamTask task = getTaskById(taskId);
        task.updateStatus(status);
    }

    @Transactional
    public void deleteTask(String email, Long taskId) {
        TeamTask task = getTaskById(taskId);
        taskRepository.delete(task);
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.MEMBER_NOT_FOUND));
    }

    private Team getTeamById(Long teamId) {
        return teamRepository.findByIdAndNotDeleted(teamId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.TEAM_NOT_FOUND));
    }

    private TeamTask getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.ENTITY_NOT_FOUND));
    }

    private void validateTeamMember(Member member, Team team) {
        boolean isLeader = team.getLeader() != null && team.getLeader().getId().equals(member.getId());
        boolean isActiveMember = teamMemberRepository.existsActiveMember(team, member);
        if (!isLeader && !isActiveMember) {
            throw new BusinessException(GlobalErrorCode.NOT_MEMBER);
        }
    }
}
