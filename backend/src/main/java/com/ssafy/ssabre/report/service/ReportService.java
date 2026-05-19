package com.ssafy.ssabre.report.service;

import com.ssafy.ssabre.comment.entity.Comment;
import com.ssafy.ssabre.comment.repository.CommentRepository;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.MemberRepository;
import com.ssafy.ssabre.post.entity.Post;
import com.ssafy.ssabre.post.repository.PostRepository;
import com.ssafy.ssabre.report.dto.ReportCreateRequest;
import com.ssafy.ssabre.report.dto.ReportCreateResponse;
import com.ssafy.ssabre.report.dto.ReportResponse;
import com.ssafy.ssabre.report.entity.Report;
import com.ssafy.ssabre.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public ReportCreateResponse createReport(ReportCreateRequest request, String email) {
        Member reporter = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Reporter not found"));

        Post post = null;
        Comment comment = null;

        switch (request.targetType().toUpperCase()) {
            case "POST" -> {
                post = postRepository.findById(request.targetId())
                        .orElseThrow(() -> new IllegalArgumentException("Post not found"));
            }
            case "COMMENT" -> {
                comment = commentRepository.findById(request.targetId())
                        .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
            }
            default -> throw new IllegalArgumentException("Invalid target type: " + request.targetType());
        }

        Report report = Report.builder()
                .reporter(reporter)
                .post(post)
                .comment(comment)
                .reason(request.reason())
                .detail(request.detail())
                .build();

        Report savedReport = reportRepository.save(report);

        return new ReportCreateResponse(savedReport.getId(), savedReport.getCreatedAt());
    }

    public List<ReportResponse> getAllReports() {
        return reportRepository.findAll().stream()
                .map(ReportResponse::from)
                .collect(Collectors.toList());
    }

    public org.springframework.data.domain.Page<ReportResponse> getAllReportsPaged(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return reportRepository.findAllNotDeleted(pageable).map(ReportResponse::from);
    }
}
