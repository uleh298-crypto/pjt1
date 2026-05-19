package com.ssafy.ssabre.board.service;

import com.ssafy.ssabre.board.dto.NoticeUpdateRequest;
import com.ssafy.ssabre.board.entity.Notice;
import com.ssafy.ssabre.board.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    /**
     * 공지사항 조회 (없으면 빈 공지 반환)
     */
    public Notice getNotice() {
        return noticeRepository.findById(1L)
                .orElse(Notice.createDefault());
    }

    /**
     * 공지사항 수정 (없으면 생성)
     */
    @Transactional
    public Notice updateNotice(NoticeUpdateRequest request) {
        Notice notice = noticeRepository.findById(1L)
                .orElse(Notice.createDefault());

        notice.update(request.content());
        return noticeRepository.save(notice);
    }
}
