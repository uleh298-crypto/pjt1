package com.ssafy.ssabre.study.service;

import com.ssafy.ssabre.global.entity.MemberRole;
import com.ssafy.ssabre.global.error.GlobalErrorCode;
import com.ssafy.ssabre.global.error.exception.BusinessException;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.MemberRepository;
import com.ssafy.ssabre.study.dto.StudyApplicationResponse;
import com.ssafy.ssabre.study.entity.Study;
import com.ssafy.ssabre.study.entity.StudyApplication;
import com.ssafy.ssabre.study.entity.StudyMember;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyApplicationService {

    private final StudyApplicationRepository studyApplicationRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyRepository studyRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 스터디별 지원 목록 조회 (리더 권한 필요)
     */
    public List<StudyApplicationResponse> getApplicationsByStudy(String email, Long studyId) {
        Member member = getMemberByEmail(email);
        Study study = getStudyById(studyId);

        validateStudyLeader(member, study);

        return studyApplicationRepository.findByStudyId(studyId).stream()
                .map(StudyApplicationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 지원 상세 조회 (리더 또는 지원자 본인만 조회 가능)
     */
    public StudyApplicationResponse getApplication(String email, Long applicationId) {
        Member member = getMemberByEmail(email);
        StudyApplication application = studyApplicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.APPLICATION_NOT_FOUND));

        // 리더이거나 지원자 본인인지 확인
        boolean isLeader = application.getStudy().getLeader() != null && application.getStudy().getLeader().getId().equals(member.getId());
        boolean isApplicant = application.getPortfolio().getMember().getId().equals(member.getId());

        if (!isLeader && !isApplicant) {
            throw new BusinessException(GlobalErrorCode.UNAUTHORIZED_ACTION);
        }

        return StudyApplicationResponse.from(application);
    }

    /**
     * 지원 수락
     */
    @Transactional
    public void acceptApplication(String email, Long applicationId) {
        Member member = getMemberByEmail(email);
        StudyApplication application = getApplicationById(applicationId);
        Study study = application.getStudy();

        // 리더 권한 확인
        validateStudyLeader(member, study);

        // 이미 처리된 지원인지 확인
        if (!application.isPending()) {
            throw new BusinessException(GlobalErrorCode.INVALID_APPLICATION_STATUS);
        }

        Member applicant = application.getPortfolio().getMember();

        // 이미 멤버인지 확인
        if (studyMemberRepository.existsActiveMember(study, applicant)) {
            throw new BusinessException(GlobalErrorCode.ALREADY_MEMBER);
        }

        // Capacity 확인
        validateCapacity(study);

        // 지원 승인
        application.approve();

        // 기존 멤버십 확인 (탈퇴한 경우 재활성화)
        StudyMember existingMember = studyMemberRepository.findByStudyAndMember(study, applicant)
                .orElse(null);

        if (existingMember != null) {
            // 기존 멤버십 재활성화
            existingMember.reactivate();
        } else {
            // 새 멤버 추가
            studyMemberRepository.save(StudyMember.create(study, applicant, MemberRole.MEMBER));
        }

        // 새소식 저장 (DB)
        notificationService.send(applicant.getId(), NotificationType.APPLICATION,
                study.getTitle(), "'" + study.getTitle() + "' 스터디 지원이 수락되었습니다.",
                "/studies/" + study.getId());

        // FCM Data 메시지 발송 (트랜잭션 커밋 후)
        eventPublisher.publishEvent(new FCMApplicationEvent(
                applicant.getId(),
                FCMDataType.APPLICATION_ACCEPTED,
                "STUDY",
                study.getId(),
                study.getTitle(),
                Instant.now()
        ));
    }

    /**
     * 지원 거절
     */
    @Transactional
    public void rejectApplication(String email, Long applicationId) {
        Member member = getMemberByEmail(email);
        StudyApplication application = getApplicationById(applicationId);
        Study study = application.getStudy();

        // 리더 권한 확인
        validateStudyLeader(member, study);

        // 이미 처리된 지원인지 확인
        if (!application.isPending()) {
            throw new BusinessException(GlobalErrorCode.INVALID_APPLICATION_STATUS);
        }

        // 지원 거절
        application.reject();

        Member applicant = application.getPortfolio().getMember();

        // 새소식 저장 (DB)
        notificationService.send(applicant.getId(), NotificationType.APPLICATION,
                study.getTitle(), "'" + study.getTitle() + "' 스터디 지원이 반려되었습니다.",
                "/studies/" + study.getId());

        // FCM Data 메시지 발송 (트랜잭션 커밋 후)
        eventPublisher.publishEvent(new FCMApplicationEvent(
                applicant.getId(),
                FCMDataType.APPLICATION_REJECTED,
                "STUDY",
                study.getId(),
                study.getTitle(),
                Instant.now()
        ));
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

    private StudyApplication getApplicationById(Long applicationId) {
        return studyApplicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.APPLICATION_NOT_FOUND));
    }

    private void validateStudyLeader(Member member, Study study) {
        if (study.getLeader() == null || !study.getLeader().getId().equals(member.getId())) {
            throw new BusinessException(GlobalErrorCode.NOT_GROUP_LEADER);
        }
    }

    private void validateCapacity(Study study) {
        if (study.getCapacity() != null) {
            long currentMembers = studyMemberRepository.countActiveMembers(study);
            if (currentMembers >= study.getCapacity()) {
                throw new BusinessException(GlobalErrorCode.CAPACITY_EXCEEDED);
            }
        }
    }

    /**
     * 내 지원 목록 조회
     */
    public List<StudyApplicationResponse> getMyApplications(String email) {
        Member member = getMemberByEmail(email);
        return studyApplicationRepository.findByMemberId(member.getId()).stream()
                .map(StudyApplicationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 지원 취소 (PENDING 상태인 경우에만 가능)
     */
    @Transactional
    public void cancelApplication(String email, Long applicationId) {
        Member member = getMemberByEmail(email);
        StudyApplication application = getApplicationById(applicationId);

        // 지원자 본인인지 확인
        if (!application.getPortfolio().getMember().getId().equals(member.getId())) {
            throw new BusinessException(GlobalErrorCode.UNAUTHORIZED_ACTION);
        }

        // PENDING 상태인지 확인
        if (!application.isPending()) {
            throw new BusinessException(GlobalErrorCode.INVALID_APPLICATION_STATUS);
        }

        // 지원 삭제
        studyApplicationRepository.delete(application);
    }
}
