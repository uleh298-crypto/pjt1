package com.ssafy.ssabre.team.service;

import com.ssafy.ssabre.global.entity.MemberRole;
import com.ssafy.ssabre.global.error.GlobalErrorCode;
import com.ssafy.ssabre.global.error.exception.BusinessException;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.MemberRepository;
import com.ssafy.ssabre.team.dto.TeamApplicationResponse;
import com.ssafy.ssabre.team.entity.Team;
import com.ssafy.ssabre.team.entity.TeamApplication;
import com.ssafy.ssabre.team.entity.TeamMember;
import com.ssafy.ssabre.team.repository.TeamApplicationRepository;
import com.ssafy.ssabre.team.repository.TeamMemberRepository;
import com.ssafy.ssabre.team.repository.TeamRepository;
import com.ssafy.ssabre.notification.dto.FCMDataType;
import com.ssafy.ssabre.notification.entity.NotificationType;
import com.ssafy.ssabre.notification.event.FCMApplicationEvent;
import com.ssafy.ssabre.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamApplicationService {

    private final TeamApplicationRepository teamApplicationRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 팀별 지원 목록 조회 (리더 권한 필요)
     */
    public List<TeamApplicationResponse> getApplicationsByTeam(String email, Long teamId) {
        Member member = getMemberByEmail(email);
        Team team = getTeamById(teamId);

        validateTeamLeader(member, team);

        return teamApplicationRepository.findByTeamId(teamId).stream()
                .map(TeamApplicationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 지원 상세 조회 (리더 또는 지원자 본인만 조회 가능)
     */
    public TeamApplicationResponse getApplication(String email, Long applicationId) {
        Member member = getMemberByEmail(email);
        TeamApplication application = teamApplicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.APPLICATION_NOT_FOUND));

        // 리더이거나 지원자 본인인지 확인
        boolean isLeader = application.getTeam().getLeader() != null && application.getTeam().getLeader().getId().equals(member.getId());
        boolean isApplicant = application.getPortfolio().getMember().getId().equals(member.getId());

        if (!isLeader && !isApplicant) {
            throw new BusinessException(GlobalErrorCode.UNAUTHORIZED_ACTION);
        }

        return TeamApplicationResponse.from(application);
    }

    /**
     * 지원 수락
     */
    @Transactional
    public void acceptApplication(String email, Long applicationId) {
        Member member = getMemberByEmail(email);
        TeamApplication application = getApplicationById(applicationId);
        Team team = application.getTeam();

        // 리더 권한 확인
        validateTeamLeader(member, team);

        // 이미 처리된 지원인지 확인
        if (!application.isPending()) {
            throw new BusinessException(GlobalErrorCode.INVALID_APPLICATION_STATUS);
        }

        Member applicant = application.getPortfolio().getMember();

        // 이미 멤버인지 확인
        if (teamMemberRepository.existsActiveMember(team, applicant)) {
            throw new BusinessException(GlobalErrorCode.ALREADY_MEMBER);
        }

        // Capacity 확인
        validateCapacity(team);

        // 지원 승인
        application.approve();

        // 기존 멤버십 확인 (탈퇴한 경우 재활성화)
        TeamMember existingMember = teamMemberRepository.findByTeamAndMember(team, applicant)
                .orElse(null);

        if (existingMember != null) {
            // 기존 멤버십 재활성화
            existingMember.reactivate();
        } else {
            // 새 멤버 추가
            teamMemberRepository.save(TeamMember.create(team, applicant, MemberRole.MEMBER));
        }

        // 새소식 저장 (DB)
        notificationService.send(applicant.getId(), NotificationType.APPLICATION,
                team.getTitle(), "'" + team.getTitle() + "' 팀 지원이 수락되었습니다.",
                "/teams/" + team.getId());

        // FCM Data 메시지 발송 (트랜잭션 커밋 후)
        eventPublisher.publishEvent(new FCMApplicationEvent(
                applicant.getId(),
                FCMDataType.APPLICATION_ACCEPTED,
                "TEAM",
                team.getId(),
                team.getTitle(),
                Instant.now()
        ));
    }

    /**
     * 지원 거절
     */
    @Transactional
    public void rejectApplication(String email, Long applicationId) {
        Member member = getMemberByEmail(email);
        TeamApplication application = getApplicationById(applicationId);
        Team team = application.getTeam();

        // 리더 권한 확인
        validateTeamLeader(member, team);

        // 이미 처리된 지원인지 확인
        if (!application.isPending()) {
            throw new BusinessException(GlobalErrorCode.INVALID_APPLICATION_STATUS);
        }

        // 지원 거절
        application.reject();

        Member applicant = application.getPortfolio().getMember();

        // 새소식 저장 (DB)
        notificationService.send(applicant.getId(), NotificationType.APPLICATION,
                team.getTitle(), "'" + team.getTitle() + "' 팀 지원이 반려되었습니다.",
                "/teams/" + team.getId());

        // FCM Data 메시지 발송 (트랜잭션 커밋 후)
        eventPublisher.publishEvent(new FCMApplicationEvent(
                applicant.getId(),
                FCMDataType.APPLICATION_REJECTED,
                "TEAM",
                team.getId(),
                team.getTitle(),
                Instant.now()
        ));
    }

    // === Private Helper Methods ===

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.MEMBER_NOT_FOUND));
    }

    private Team getTeamById(Long teamId) {
        return teamRepository.findByIdAndNotDeleted(teamId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.TEAM_NOT_FOUND));
    }

    private TeamApplication getApplicationById(Long applicationId) {
        return teamApplicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.APPLICATION_NOT_FOUND));
    }

    private void validateTeamLeader(Member member, Team team) {
        if (team.getLeader() == null || !team.getLeader().getId().equals(member.getId())) {
            throw new BusinessException(GlobalErrorCode.NOT_GROUP_LEADER);
        }
    }

    private void validateCapacity(Team team) {
        if (team.getCapacity() != null) {
            long currentMembers = teamMemberRepository.countActiveMembers(team);
            if (currentMembers >= team.getCapacity()) {
                throw new BusinessException(GlobalErrorCode.CAPACITY_EXCEEDED);
            }
        }
    }

    /**
     * 내 지원 목록 조회
     */
    public List<TeamApplicationResponse> getMyApplications(String email) {
        Member member = getMemberByEmail(email);
        return teamApplicationRepository.findByMemberId(member.getId()).stream()
                .map(TeamApplicationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 지원 취소 (PENDING 상태인 경우에만 가능)
     */
    @Transactional
    public void cancelApplication(String email, Long applicationId) {
        Member member = getMemberByEmail(email);
        TeamApplication application = getApplicationById(applicationId);

        // 지원자 본인인지 확인
        if (!application.getPortfolio().getMember().getId().equals(member.getId())) {
            throw new BusinessException(GlobalErrorCode.UNAUTHORIZED_ACTION);
        }

        // PENDING 상태인지 확인
        if (!application.isPending()) {
            throw new BusinessException(GlobalErrorCode.INVALID_APPLICATION_STATUS);
        }

        // 지원 삭제
        teamApplicationRepository.delete(application);
    }
}
