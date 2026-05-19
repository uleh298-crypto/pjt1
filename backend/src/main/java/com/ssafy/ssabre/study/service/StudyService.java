package com.ssafy.ssabre.study.service;

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
import com.ssafy.ssabre.study.dto.StudyApplyRequest;
import com.ssafy.ssabre.study.dto.StudyCreateRequest;
import com.ssafy.ssabre.study.dto.StudyMemberResponse;
import com.ssafy.ssabre.study.dto.StudyUpdateRequest;
import com.ssafy.ssabre.study.entity.Study;
import com.ssafy.ssabre.study.entity.StudyApplication;
import com.ssafy.ssabre.study.entity.StudyMember;
import com.ssafy.ssabre.study.entity.StudyType;
import com.ssafy.ssabre.study.repository.StudyApplicationRepository;
import com.ssafy.ssabre.study.repository.StudyMemberRepository;
import com.ssafy.ssabre.study.repository.StudyRepository;
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
public class StudyService {

    private final StudyRepository studyRepository;
    private final MemberRepository memberRepository;
    private final CampusRepository campusRepository;
    private final PortfolioRepository portfolioRepository;
    private final StudyApplicationRepository studyApplicationRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Study createStudy(String email, StudyCreateRequest request) {
        Member member = getMemberByEmail(email);
        Campus campus = getCampusById(request.campusId());

        // 날짜 유효성 검증
        validateDateRange(request.startDate(), request.endDate());

        Study study = Study.create(
                member, request.title(), request.type(), request.capacity(),
                request.description(), campus, request.startDate(), request.endDate());

        study = studyRepository.save(study);
        studyMemberRepository.save(StudyMember.create(study, member, MemberRole.LEADER));

        // Fetch Join으로 다시 조회하여 반환
        return studyRepository.findByIdWithLeader(study.getId()).orElse(study);
    }

    public List<Study> findAll(Long campusId, StudyType type) {
        if (campusId != null && type != null) {
            return studyRepository.findByCampusIdAndType(campusId, type);
        } else if (campusId != null) {
            return studyRepository.findByCampusId(campusId);
        } else if (type != null) {
            return studyRepository.findByType(type);
        }
        return studyRepository.findAllNotDeleted();
    }

    public Study getStudy(Long studyId) {
        return studyRepository.findByIdWithLeader(studyId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.STUDY_NOT_FOUND));
    }

    @Transactional
    public void updateStudy(String email, Long studyId, StudyUpdateRequest request) {
        Member member = getMemberByEmail(email);
        Study study = getStudyById(studyId);

        validateLeadership(study, member);

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

        study.update(request.title(), request.type(), request.capacity(),
                request.description(), status,
                campus, request.startDate(), request.endDate());
    }

    @Transactional
    public void deleteStudy(String email, Long studyId) {
        Member member = getMemberByEmail(email);
        Study study = getStudyById(studyId);

        validateLeadership(study, member);

        study.softDelete();
    }

    @Transactional
    public void applyStudy(String email, Long studyId, StudyApplyRequest request) {
        Member member = getMemberByEmail(email);
        Study study = getStudyById(studyId);
        Portfolio portfolio = getPortfolioById(request.portfolioId());

        // 포트폴리오 소유권 검증
        validatePortfolioOwnership(portfolio, member);

        // 스터디가 모집 중인지 확인
        if (!study.isOpen()) {
            throw new BusinessException(GlobalErrorCode.GROUP_NOT_OPEN);
        }

        // 이미 멤버인지 확인
        if (studyMemberRepository.existsActiveMember(study, member)) {
            throw new BusinessException(GlobalErrorCode.ALREADY_MEMBER);
        }

        // 이미 지원했는지 확인 (중복 지원 방지)
        if (studyApplicationRepository.existsPendingApplication(study, member)) {
            throw new BusinessException(GlobalErrorCode.DUPLICATE_APPLICATION);
        }

        StudyApplication application = StudyApplication.create(
                study, portfolio, request.title(), request.message(), request.position());

        studyApplicationRepository.save(application);

        Long leaderId = study.getLeader() != null ? study.getLeader().getId() : null;

        // 새소식 저장 (DB)
        if (leaderId == null) {
            throw new BusinessException(GlobalErrorCode.NOT_GROUP_LEADER);
        }
        notificationService.send(leaderId, NotificationType.APPLICATION,
                study.getTitle(), "'" + study.getTitle() + "' 스터디에 새로운 지원자가 있습니다.",
                "/studies/" + study.getId() + "/applications");

        // FCM Data 메시지 발송 (트랜잭션 커밋 후)
        eventPublisher.publishEvent(new FCMApplicationEvent(
                leaderId,
                FCMDataType.APPLICATION_NEW,
                "STUDY",
                study.getId(),
                study.getTitle(),
                Instant.now()
        ));
    }

    public List<Study> getMyStudies(String email) {
        Member member = getMemberByEmail(email);
        return studyRepository.findMyStudies(member.getId());
    }

    /**
     * 스터디 멤버 목록 조회
     */
    public List<StudyMemberResponse> getStudyMembers(Long studyId) {
        // 스터디 존재 여부 확인
        getStudyById(studyId);
        List<StudyMember> members = studyMemberRepository.findActiveMembersByStudyId(studyId);

        if (members.isEmpty()) {
            return List.of();
        }

        // 멤버들의 ID 리스트 추출
        List<Long> memberIds = members.stream()
                .map(sm -> sm.getMember().getId())
                .collect(Collectors.toList());

        // 한 번의 쿼리로 모든 멤버의 포트폴리오 조회 (N+1 방지)
        List<Portfolio> allPortfolios = portfolioRepository.findByMemberIdIn(memberIds);

        // memberId -> 첫 번째 portfolioId 매핑
        Map<Long, Long> memberPortfolioMap = new java.util.HashMap<>();
        for (Long memberId : memberIds) {
            memberPortfolioMap.put(memberId, null);
        }
        for (Portfolio portfolio : allPortfolios) {
            Long memberId = portfolio.getMember().getId();
            if (memberPortfolioMap.get(memberId) == null) {
                memberPortfolioMap.put(memberId, portfolio.getId());
            }
        }

        return members.stream()
                .map(sm -> StudyMemberResponse.from(sm, memberPortfolioMap.get(sm.getMember().getId())))
                .collect(Collectors.toList());
    }

    /**
     * 스터디 리더 여부 검증 (외부에서 사용 가능)
     */
    public void validateLeadershipByEmail(String email, Long studyId) {
        Member member = getMemberByEmail(email);
        Study study = getStudyById(studyId);
        validateLeadership(study, member);
    }

    /**
     * 스터디 멤버 추방 (리더만 가능)
     */
    @Transactional
    public void kickMember(String email, Long studyId, Long memberId) {
        Member leader = getMemberByEmail(email);
        Study study = getStudyById(studyId);

        // 리더 권한 확인
        validateLeadership(study, leader);

        // 자기 자신을 추방하려는 경우
        if (leader.getId().equals(memberId)) {
            throw new BusinessException(GlobalErrorCode.CANNOT_KICK_SELF);
        }

        // 리더를 추방하려는 경우
        if (study.getLeader() != null && study.getLeader().getId().equals(memberId)) {
            throw new BusinessException(GlobalErrorCode.CANNOT_KICK_LEADER);
        }

        Member targetMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.MEMBER_NOT_FOUND));

        StudyMember studyMember = studyMemberRepository.findByStudyAndMember(study, targetMember)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.MEMBER_NOT_IN_STUDY));

        // 이미 탈퇴한 멤버인지 확인
        if (!studyMember.isActive()) {
            throw new BusinessException(GlobalErrorCode.MEMBER_NOT_IN_STUDY);
        }

        // 멤버 추방 (soft delete)
        studyMember.quit();
    }

    /**
     * 스터디 자진 탈퇴
     */
    @Transactional
    public void leaveStudy(String email, Long studyId) {
        Member member = getMemberByEmail(email);
        Study study = getStudyById(studyId);

        // 리더는 탈퇴할 수 없음
        if (study.getLeader() != null && study.getLeader().getId().equals(member.getId())) {
            throw new BusinessException(GlobalErrorCode.LEADER_CANNOT_LEAVE);
        }

        StudyMember studyMember = studyMemberRepository.findByStudyAndMember(study, member)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.MEMBER_NOT_IN_STUDY));

        // 이미 탈퇴한 멤버인지 확인
        if (!studyMember.isActive()) {
            throw new BusinessException(GlobalErrorCode.MEMBER_NOT_IN_STUDY);
        }

        // 자진 탈퇴 (soft delete)
        studyMember.quit();
    }

    // === Private Helper Methods ===

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.MEMBER_NOT_FOUND));
    }

    private Study getStudyById(Long studyId) {
        return studyRepository.findByIdAndNotDeleted(studyId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.STUDY_NOT_FOUND));
    }

    private Campus getCampusById(Long campusId) {
        return campusRepository.findById(campusId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.CAMPUS_NOT_FOUND));
    }

    private Portfolio getPortfolioById(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.PORTFOLIO_NOT_FOUND));
    }

    private void validateLeadership(Study study, Member member) {
        if (study.getLeader() == null || !study.getLeader().getId().equals(member.getId())) {
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
