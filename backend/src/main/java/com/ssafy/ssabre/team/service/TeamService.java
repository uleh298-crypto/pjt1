package com.ssafy.ssabre.team.service;

import com.ssafy.ssabre.campus.entity.Campus;
import com.ssafy.ssabre.campus.repository.CampusRepository;
import com.ssafy.ssabre.global.entity.GroupStatus;
import com.ssafy.ssabre.global.entity.MemberRole;
import com.ssafy.ssabre.global.error.GlobalErrorCode;
import com.ssafy.ssabre.global.error.exception.BusinessException;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.MemberRepository;
import com.ssafy.ssabre.portfolio.entity.Portfolio;
import com.ssafy.ssabre.portfolio.repository.PortfolioRepository;
import com.ssafy.ssabre.team.dto.TeamApplyRequest;
import com.ssafy.ssabre.team.dto.TeamCreateRequest;
import com.ssafy.ssabre.team.dto.TeamMemberResponse;
import com.ssafy.ssabre.team.dto.TeamUpdateRequest;
import com.ssafy.ssabre.team.entity.Team;
import com.ssafy.ssabre.team.entity.TeamApplication;
import com.ssafy.ssabre.team.entity.TeamMember;
import com.ssafy.ssabre.team.entity.TeamType;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamApplicationRepository teamApplicationRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MemberRepository memberRepository;
    private final PortfolioRepository portfolioRepository;
    private final CampusRepository campusRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Team createTeam(String email, TeamCreateRequest request) {
        Member member = getMemberByEmail(email);
        Campus campus = getCampusById(request.campusId());

        // 날짜 유효성 검증
        validateDateRange(request.startDate(), request.endDate());

        Team team = teamRepository.save(
                Team.create(member, request.title(), request.type(), request.capacity(),
                        request.description(), campus, request.startDate(), request.endDate()));

        teamMemberRepository.save(TeamMember.create(team, member, MemberRole.LEADER));

        // Fetch Join으로 다시 조회하여 반환
        return teamRepository.findByIdWithLeader(team.getId()).orElse(team);
    }

    public List<Team> findAll(Long campusId, TeamType type) {
        if (campusId != null && type != null) {
            return teamRepository.findByCampusIdAndType(campusId, type);
        } else if (campusId != null) {
            return teamRepository.findByCampusId(campusId);
        } else if (type != null) {
            return teamRepository.findByType(type);
        }
        return teamRepository.findAllNotDeleted();
    }

    @Transactional
    public void applyTeam(String email, Long teamId, TeamApplyRequest request) {
        Member member = getMemberByEmail(email);
        Team team = getTeamById(teamId);
        Portfolio portfolio = getPortfolioById(request.portfolioId());

        // 포트폴리오 소유권 검증
        validatePortfolioOwnership(portfolio, member);

        // 팀이 모집 중인지 확인
        if (!team.isOpen()) {
            throw new BusinessException(GlobalErrorCode.GROUP_NOT_OPEN);
        }

        // 이미 멤버인지 확인
        if (teamMemberRepository.existsActiveMember(team, member)) {
            throw new BusinessException(GlobalErrorCode.ALREADY_MEMBER);
        }

        // 이미 지원했는지 확인 (중복 지원 방지)
        if (teamApplicationRepository.existsPendingApplication(team, member)) {
            throw new BusinessException(GlobalErrorCode.DUPLICATE_APPLICATION);
        }

        teamApplicationRepository.save(
                TeamApplication.create(team, portfolio, request.title(), request.message(), request.position()));

        Long leaderId = team.getLeader() != null ? team.getLeader().getId() : null;

        // 새소식 저장 (DB)
        if (leaderId == null) {
            throw new BusinessException(GlobalErrorCode.NOT_GROUP_LEADER);
        }
        notificationService.send(leaderId, NotificationType.APPLICATION,
                team.getTitle(), "'" + team.getTitle() + "' 팀에 새로운 지원자가 있습니다.",
                "/teams/" + team.getId() + "/applications");

        // FCM Data 메시지 발송 (트랜잭션 커밋 후)
        eventPublisher.publishEvent(new FCMApplicationEvent(
                leaderId,
                FCMDataType.APPLICATION_NEW,
                "TEAM",
                team.getId(),
                team.getTitle(),
                Instant.now()
        ));
    }

    @Transactional
    public void updateTeam(String email, Long teamId, TeamUpdateRequest request) {
        Member member = getMemberByEmail(email);
        Team team = getTeamById(teamId);

        validateLeadership(team, member);

        Campus campus = null;
        if (request.campusId() != null) {
            campus = getCampusById(request.campusId());
        }

        GroupStatus status = null;
        if (request.status() != null) {
            try {
                status = GroupStatus.valueOf(request.status().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(GlobalErrorCode.INVALID_INPUT);
            }
        }

        // 날짜 유효성 검증 (수정 시)
        validateDateRange(request.startDate(), request.endDate());

        team.update(request.title(), request.type(), request.capacity(), request.description(),
                status, campus, request.startDate(), request.endDate());
    }

    @Transactional
    public void deleteTeam(String email, Long teamId) {
        Member member = getMemberByEmail(email);
        Team team = getTeamById(teamId);

        validateLeadership(team, member);

        team.softDelete();
    }

    public Team getTeam(Long teamId) {
        return teamRepository.findByIdWithLeader(teamId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.TEAM_NOT_FOUND));
    }

    public List<Team> getMyTeams(String email) {
        Member member = getMemberByEmail(email);
        return teamRepository.findMyTeams(member.getId());
    }

    /**
     * 팀 멤버 목록 조회
     */
    public List<TeamMemberResponse> getTeamMembers(Long teamId) {
        // 팀 존재 여부 확인
        getTeamById(teamId);
        List<TeamMember> members = teamMemberRepository.findActiveMembersByTeamId(teamId);

        if (members.isEmpty()) {
            return List.of();
        }

        // 멤버들의 ID 리스트 추출
        List<Long> memberIds = members.stream()
                .map(tm -> tm.getMember().getId())
                .collect(Collectors.toList());

        // 한 번의 쿼리로 모든 멤버의 포트폴리오 조회 (N+1 방지)
        List<Portfolio> allPortfolios = portfolioRepository.findByMemberIdIn(memberIds);

        // memberId -> 첫 번째 portfolioId 매핑 (HashMap은 null value 허용)
        Map<Long, Long> memberPortfolioMap = new java.util.HashMap<>();
        for (Long memberId : memberIds) {
            memberPortfolioMap.put(memberId, null); // 기본값 null
        }
        for (Portfolio portfolio : allPortfolios) {
            Long memberId = portfolio.getMember().getId();
            // 아직 매핑되지 않은 경우에만 (첫 번째 포트폴리오만 저장)
            if (memberPortfolioMap.get(memberId) == null) {
                memberPortfolioMap.put(memberId, portfolio.getId());
            }
        }

        return members.stream()
                .map(tm -> TeamMemberResponse.from(tm, memberPortfolioMap.get(tm.getMember().getId())))
                .collect(Collectors.toList());
    }

    /**
     * 팀 리더 여부 검증 (외부에서 사용 가능)
     */
    public void validateLeadershipByEmail(String email, Long teamId) {
        Member member = getMemberByEmail(email);
        Team team = getTeamById(teamId);
        validateLeadership(team, member);
    }

    /**
     * 팀 멤버 추방 (리더만 가능)
     */
    @Transactional
    public void kickMember(String email, Long teamId, Long memberId) {
        Member leader = getMemberByEmail(email);
        Team team = getTeamById(teamId);

        // 리더 권한 확인
        validateLeadership(team, leader);

        // 자기 자신을 추방하려는 경우
        if (leader.getId().equals(memberId)) {
            throw new BusinessException(GlobalErrorCode.CANNOT_KICK_SELF);
        }

        // 리더를 추방하려는 경우 (팀 리더가 다른 리더를 추방하는 상황은 없지만 안전장치)
        if (team.getLeader() != null && team.getLeader().getId().equals(memberId)) {
            throw new BusinessException(GlobalErrorCode.CANNOT_KICK_LEADER);
        }

        Member targetMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.MEMBER_NOT_FOUND));

        TeamMember teamMember = teamMemberRepository.findByTeamAndMember(team, targetMember)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.MEMBER_NOT_IN_TEAM));

        // 이미 탈퇴한 멤버인지 확인
        if (!teamMember.isActive()) {
            throw new BusinessException(GlobalErrorCode.MEMBER_NOT_IN_TEAM);
        }

        // 멤버 추방 (soft delete)
        teamMember.quit();
    }

    /**
     * 팀 자진 탈퇴
     */
    @Transactional
    public void leaveTeam(String email, Long teamId) {
        Member member = getMemberByEmail(email);
        Team team = getTeamById(teamId);

        // 리더는 탈퇴할 수 없음
        if (team.getLeader() != null && team.getLeader().getId().equals(member.getId())) {
            throw new BusinessException(GlobalErrorCode.LEADER_CANNOT_LEAVE);
        }

        TeamMember teamMember = teamMemberRepository.findByTeamAndMember(team, member)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.MEMBER_NOT_IN_TEAM));

        // 이미 탈퇴한 멤버인지 확인
        if (!teamMember.isActive()) {
            throw new BusinessException(GlobalErrorCode.MEMBER_NOT_IN_TEAM);
        }

        // 자진 탈퇴 (soft delete)
        teamMember.quit();
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

    private Campus getCampusById(Long campusId) {
        return campusRepository.findById(campusId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.CAMPUS_NOT_FOUND));
    }

    private Portfolio getPortfolioById(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.PORTFOLIO_NOT_FOUND));
    }

    private void validateLeadership(Team team, Member member) {
        if (team.getLeader() == null || !team.getLeader().getId().equals(member.getId())) {
            throw new BusinessException(GlobalErrorCode.NOT_GROUP_LEADER);
        }
    }

    private void validatePortfolioOwnership(Portfolio portfolio, Member member) {
        if (!portfolio.getMember().getId().equals(member.getId())) {
            throw new BusinessException(GlobalErrorCode.INVALID_PORTFOLIO_OWNER);
        }
    }

    private void validateDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(GlobalErrorCode.INVALID_DATE_RANGE);
        }
    }
}
