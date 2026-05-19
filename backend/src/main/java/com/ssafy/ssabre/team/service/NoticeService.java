package com.ssafy.ssabre.team.service;

import com.ssafy.ssabre.global.error.GlobalErrorCode;
import com.ssafy.ssabre.global.error.exception.BusinessException;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.MemberRepository;
import com.ssafy.ssabre.team.dto.NoticeResponse;
import com.ssafy.ssabre.team.entity.Notice;
import com.ssafy.ssabre.team.entity.Team;
import com.ssafy.ssabre.team.repository.TeamNoticeRepository;
import com.ssafy.ssabre.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("teamNoticeService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final TeamNoticeRepository noticeRepository;
    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;

    public List<NoticeResponse> getNotices(Long teamId) {
        return noticeRepository.findByTeamIdOrderByIsPinnedDescCreatedAtDesc(teamId).stream()
                .map(NoticeResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createNotice(String email, Long teamId, String title, String content, Boolean isPinned) {
        Member member = getMemberByEmail(email);
        Team team = getTeamById(teamId);

        validateTeamLeader(member, team);

        noticeRepository.save(Notice.create(team, title, content, isPinned));
    }

    @Transactional
    public void updateNotice(String email, Long noticeId, String title, String content, Boolean isPinned) {
        Member member = getMemberByEmail(email);
        Notice notice = getNoticeById(noticeId);

        validateTeamLeader(member, notice.getTeam());

        notice.update(title, content, isPinned);
    }

    @Transactional
    public void deleteNotice(String email, Long noticeId) {
        Member member = getMemberByEmail(email);
        Notice notice = getNoticeById(noticeId);

        validateTeamLeader(member, notice.getTeam());

        noticeRepository.delete(notice);
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.MEMBER_NOT_FOUND));
    }

    private Team getTeamById(Long teamId) {
        return teamRepository.findByIdAndNotDeleted(teamId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.TEAM_NOT_FOUND));
    }

    private Notice getNoticeById(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.ENTITY_NOT_FOUND));
    }

    private void validateTeamLeader(Member member, Team team) {
        if (team.getLeader() == null || !team.getLeader().getId().equals(member.getId())) {
            throw new BusinessException(GlobalErrorCode.NOT_GROUP_LEADER);
        }
    }
}
