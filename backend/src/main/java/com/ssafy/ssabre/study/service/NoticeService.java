package com.ssafy.ssabre.study.service;

import com.ssafy.ssabre.global.error.GlobalErrorCode;
import com.ssafy.ssabre.global.error.exception.BusinessException;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.MemberRepository;
import com.ssafy.ssabre.study.dto.NoticeResponse;
import com.ssafy.ssabre.study.entity.Notice;
import com.ssafy.ssabre.study.entity.Study;
import com.ssafy.ssabre.study.repository.StudyNoticeRepository;
import com.ssafy.ssabre.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("studyNoticeService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final StudyNoticeRepository noticeRepository;
    private final StudyRepository studyRepository;
    private final MemberRepository memberRepository;

    public List<NoticeResponse> getNotices(Long studyId) {
        return noticeRepository.findByStudyIdOrderByIsPinnedDescCreatedAtDesc(studyId).stream()
                .map(NoticeResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createNotice(String email, Long studyId, String title, String content, Boolean isPinned) {
        Member member = getMemberByEmail(email);
        Study study = getStudyById(studyId);

        validateStudyLeader(member, study);

        noticeRepository.save(Notice.create(study, title, content, isPinned));
    }

    @Transactional
    public void updateNotice(String email, Long noticeId, String title, String content, Boolean isPinned) {
        Member member = getMemberByEmail(email);
        Notice notice = getNoticeById(noticeId);

        validateStudyLeader(member, notice.getStudy());

        notice.update(title, content, isPinned);
    }

    @Transactional
    public void deleteNotice(String email, Long noticeId) {
        Member member = getMemberByEmail(email);
        Notice notice = getNoticeById(noticeId);

        validateStudyLeader(member, notice.getStudy());

        noticeRepository.delete(notice);
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.MEMBER_NOT_FOUND));
    }

    private Study getStudyById(Long studyId) {
        return studyRepository.findByIdAndNotDeleted(studyId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.STUDY_NOT_FOUND));
    }

    private Notice getNoticeById(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.ENTITY_NOT_FOUND));
    }

    private void validateStudyLeader(Member member, Study study) {
        if (study.getLeader() == null || !study.getLeader().getId().equals(member.getId())) {
            throw new BusinessException(GlobalErrorCode.NOT_GROUP_LEADER);
        }
    }
}
