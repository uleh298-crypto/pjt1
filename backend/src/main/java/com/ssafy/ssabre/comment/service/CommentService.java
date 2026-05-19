package com.ssafy.ssabre.comment.service;

import com.ssafy.ssabre.comment.dto.CommentLikeResponse;
import com.ssafy.ssabre.comment.entity.Comment;
import com.ssafy.ssabre.comment.entity.CommentLike;
import com.ssafy.ssabre.comment.repository.CommentLikeRepository;
import com.ssafy.ssabre.comment.repository.CommentRepository;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.MemberRepository;
import com.ssafy.ssabre.notification.dto.FCMDataType;
import com.ssafy.ssabre.notification.entity.NotificationType;
import com.ssafy.ssabre.notification.event.FCMCommentEvent;
import com.ssafy.ssabre.notification.service.NotificationService;
import com.ssafy.ssabre.post.dto.AnonResponse;
import com.ssafy.ssabre.post.dto.CommentResponse;
import com.ssafy.ssabre.post.dto.ReplyResponse;
import com.ssafy.ssabre.post.entity.Post;
import com.ssafy.ssabre.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;

    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Comment save(com.ssafy.ssabre.comment.dto.CommentCreateRequest request, String email) {
        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        Comment comment = Comment.builder()
                .content(request.content())
                .post(post)
                .member(member)
                .parentId(request.parentId())
                .build();

        Comment savedComment = commentRepository.save(comment);
        post.incrementCommentCount();

        // Async Censorship Event
        eventPublisher.publishEvent(new com.ssafy.ssabre.comment.event.CommentCreatedEvent(savedComment.getId(),
                savedComment.getContent()));

        Instant sentAt = Instant.now();

        // 알림 발송: 게시글 작성자에게 (COMMENT)
        Long postAuthorId = post.getMember() != null ? post.getMember().getId() : null;
        if (postAuthorId != null && !postAuthorId.equals(member.getId())) {
            // 기존 Notification 알림 (DB 저장 + 기본 FCM)
            notificationService.send(postAuthorId, NotificationType.COMMENT,
                    post.getTitle(), "내 글에 새 댓글이 달렸습니다.", "/posts/" + post.getId());

            // FCM Data 메시지 발송 (트랜잭션 커밋 후)
            eventPublisher.publishEvent(new FCMCommentEvent(
                    postAuthorId,
                    FCMDataType.POST_COMMENT,
                    post.getId(),
                    savedComment.getId(),
                    sentAt));
        }

        // 알림 발송: 부모 댓글 작성자에게 (REPLY) - 대댓글일 경우
        if (request.parentId() != null) {
            commentRepository.findById(request.parentId()).ifPresent(parent -> {
                Long parentAuthorId = parent.getMember() != null ? parent.getMember().getId() : null;
                if (parentAuthorId != null && !parentAuthorId.equals(member.getId())) {
                    // 기존 Notification 알림 (DB 저장 + 기본 FCM)
                    notificationService.send(parentAuthorId, NotificationType.REPLY,
                            post.getTitle(), "내 댓글에 답글이 달렸습니다.", "/posts/" + post.getId());

                    // FCM Data 메시지 발송 (트랜잭션 커밋 후)
                    eventPublisher.publishEvent(new FCMCommentEvent(
                            parentAuthorId,
                            FCMDataType.COMMENT_REPLY,
                            post.getId(),
                            savedComment.getId(),
                            sentAt));
                }
            });
        }

        return savedComment;
    }

    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }

    @Transactional
    public CommentResponse update(Long id, com.ssafy.ssabre.comment.dto.CommentUpdateRequest request, String email) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (comment.getMember() == null || !comment.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("본인의 댓글만 수정할 수 있습니다.");
        }

        // Blocking check removed -> Async Event
        comment.update(request.content());

        // Async Censorship Event
        eventPublisher.publishEvent(
                new com.ssafy.ssabre.comment.event.CommentUpdatedEvent(comment.getId(), comment.getContent()));

        Long postId = comment.getPost().getId();
        Long postAuthorId = comment.getPost().getMember() != null ? comment.getPost().getMember().getId() : null;
        Long currentMemberId = member.getId();

        // 익명 번호 계산
        Map<Long, Integer> anonMap = new HashMap<>();
        int[] anonCounter = { 0 };
        buildAnonMap(postId, anonMap, anonCounter);

        boolean isLiked = commentLikeRepository.existsByCommentIdAndMemberId(comment.getId(), currentMemberId);
        Long commentMemberId = comment.getMember() != null ? comment.getMember().getId() : null;
        AnonResponse anon = buildAnonResponse(commentMemberId, currentMemberId, postAuthorId, anonMap,
                anonCounter);

        // 대댓글 목록
        List<Comment> replies = commentRepository.findByParentIdAndDeletedAtIsNullOrderByCreatedAtAsc(comment.getId());
        List<ReplyResponse> replyResponses = replies.stream()
                .map(reply -> buildReplyResponse(reply, currentMemberId, postAuthorId, anonMap, anonCounter))
                .toList();

        return new CommentResponse(
                comment.getId(),
                comment.getCreatedAt(),
                comment.getContent(),
                comment.getLikeCount(),
                isLiked,
                comment.getIsBlinded(),
                false,
                anon,
                replyResponses);
    }

    @Transactional
    public void blindComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        comment.setBlinded(true);
        commentRepository.save(comment);
    }

    private void buildAnonMap(Long postId, Map<Long, Integer> anonMap, int[] anonCounter) {
        List<Comment> existingComments = commentRepository
                .findByPostIdAndParentIdIsNullAndDeletedAtIsNullOrderByCreatedAtAsc(postId);
        for (Comment c : existingComments) {
            Long cMemberId = c.getMember() != null ? c.getMember().getId() : null;
            if (cMemberId != null && !anonMap.containsKey(cMemberId)) {
                anonMap.put(cMemberId, ++anonCounter[0]);
            }
            List<Comment> replies = commentRepository.findByParentIdAndDeletedAtIsNullOrderByCreatedAtAsc(c.getId());
            for (Comment r : replies) {
                Long rMemberId = r.getMember() != null ? r.getMember().getId() : null;
                if (rMemberId != null && !anonMap.containsKey(rMemberId)) {
                    anonMap.put(rMemberId, ++anonCounter[0]);
                }
            }
        }
    }

    private AnonResponse buildAnonResponse(Long memberId, Long currentMemberId, Long postAuthorId,
            Map<Long, Integer> anonMap, int[] anonCounter) {
        if (memberId == null) {
            return new AnonResponse("탈퇴한 사용자", false, false);
        }

        boolean isAuthor = memberId.equals(postAuthorId);
        boolean isMine = memberId.equals(currentMemberId);

        if (!anonMap.containsKey(memberId)) {
            anonMap.put(memberId, ++anonCounter[0]);
        }
        int anonNumber = anonMap.get(memberId);

        String name = isAuthor ? "싸용자(작성자)" : "싸용자" + anonNumber;
        return new AnonResponse(name, isAuthor, isMine);
    }

    private ReplyResponse buildReplyResponse(Comment reply, Long currentMemberId, Long postAuthorId,
            Map<Long, Integer> anonMap, int[] anonCounter) {
        boolean isLiked = commentLikeRepository.existsByCommentIdAndMemberId(reply.getId(), currentMemberId);
        Long replyMemberId = reply.getMember() != null ? reply.getMember().getId() : null;
        AnonResponse anon = buildAnonResponse(replyMemberId, currentMemberId, postAuthorId, anonMap,
                anonCounter);

        return new ReplyResponse(
                reply.getId(),
                reply.getCreatedAt(),
                reply.getContent(),
                reply.getLikeCount(),
                isLiked,
                reply.getIsBlinded(),
                false,
                anon);
    }

    @Transactional
    public void delete(Long id, String email) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (comment.getMember() == null || !comment.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("본인의 댓글만 삭제할 수 있습니다.");
        }

        Post post = comment.getPost();
        comment.delete();
        post.decrementCommentCount();
    }

    @Transactional
    public CommentLikeResponse like(Long commentId, String email) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        boolean alreadyLiked = commentLikeRepository.existsByCommentIdAndMemberId(commentId, member.getId());

        if (!alreadyLiked) {
            CommentLike commentLike = new CommentLike(member.getId(), commentId);
            commentLikeRepository.save(commentLike);
            comment.incrementLikeCount();
        }

        return new CommentLikeResponse(true, comment.getLikeCount());
    }

    @Transactional
    public CommentLikeResponse unlike(Long commentId, String email) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        boolean alreadyLiked = commentLikeRepository.existsByCommentIdAndMemberId(commentId, member.getId());

        if (alreadyLiked) {
            commentLikeRepository.deleteByCommentIdAndMemberId(commentId, member.getId());
            comment.decrementLikeCount();
        }

        return new CommentLikeResponse(false, comment.getLikeCount());
    }

}
