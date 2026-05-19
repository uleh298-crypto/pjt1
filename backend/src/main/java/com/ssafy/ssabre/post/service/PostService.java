package com.ssafy.ssabre.post.service;

import com.ssafy.ssabre.board.entity.Board;
import com.ssafy.ssabre.board.repository.BoardRepository;
import com.ssafy.ssabre.comment.entity.Comment;
import com.ssafy.ssabre.comment.repository.CommentLikeRepository;
import com.ssafy.ssabre.comment.repository.CommentRepository;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.MemberRepository;
import com.ssafy.ssabre.post.dto.*;
import com.ssafy.ssabre.post.entity.*;
import com.ssafy.ssabre.post.repository.*;
import com.ssafy.ssabre.upload.service.UploadService;
import com.ssafy.ssabre.global.service.AiCensorshipService;
import com.ssafy.ssabre.notification.dto.FCMDataType;
import com.ssafy.ssabre.notification.entity.NotificationType;
import com.ssafy.ssabre.notification.event.FCMCommentEvent;
import com.ssafy.ssabre.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final PostLikeRepository postLikeRepository;
    private final ScrapRepository scrapRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final VoteRepository voteRepository;
    private final VoteItemRepository voteItemRepository;
    private final VoteRecordRepository voteRecordRepository;
    private final UploadService uploadService;
    private final SearchHistoryRepository searchHistoryRepository;
    private final AiCensorshipService aiCensorshipService;
    private final NotificationService notificationService;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Transactional
    public PostResponse save(PostCreateRequest request, String email) {
        Board board = boardRepository.findById(request.boardId())
                .orElseThrow(() -> new IllegalArgumentException("Board not found"));

        // AI Censorship is now ASYNC via EventListener
        // Default to visible (Optimistic UI)

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .board(board)
                .member(member)
                .viewCount(0)
                .likeCount(0)
                .commentCount(0)
                .isBlinded(false) // Default false
                .build();

        Post savedPost = postRepository.save(post);

        List<PostImage> savedImages = Collections.emptyList();
        if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
            try {
                String targetFolder = "post/" + savedPost.getId();
                List<String> movedUrls = uploadService.moveFromTemp(request.imageUrls(), targetFolder);

                savedImages = movedUrls.stream()
                        .map(imageUrl -> {
                            PostImage postImage = PostImage.builder()
                                    .post(savedPost)
                                    .imageUrl(imageUrl)
                                    .build();
                            return postImageRepository.save(postImage);
                        })
                        .toList();
            } catch (IOException e) {
                log.error("Failed to move images for post {}", savedPost.getId(), e);
            }
        }

        // 투표 생성
        if (request.poll() != null) {
            Vote vote = Vote.builder()
                    .post(savedPost)
                    .title(request.poll().title())
                    .build();
            Vote savedVote = voteRepository.save(vote);

            List<String> options = request.poll().options();
            for (int i = 0; i < options.size(); i++) {
                VoteItem voteItem = VoteItem.builder()
                        .vote(savedVote)
                        .content(options.get(i))
                        .itemOrder(i + 1)
                        .build();
                voteItemRepository.save(voteItem);
            }
        }

        // Publish Event for Async Censorship
        eventPublisher.publishEvent(new com.ssafy.ssabre.post.event.PostCreatedEvent(savedPost.getId(),
                savedPost.getTitle(), savedPost.getContent()));

        return PostResponse.from(savedPost, savedImages, member.getId());
    }

    @Transactional
    public void blindPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        post.setBlinded(true);
        postRepository.save(post); // Ensure update
    }

    public List<PostResponse> findAll() {
        return postRepository.findByDeletedAtIsNullOrderByCreatedAtDesc().stream()
                .map(post -> {
                    List<PostImage> images = postImageRepository.findByPostIdAndDeletedAtIsNull(post.getId());
                    return PostResponse.from(post, images);
                })
                .toList();
    }

    public List<PostResponse> findByBoardId(Long boardId) {
        return postRepository.findByBoardIdAndDeletedAtIsNullOrderByCreatedAtDesc(boardId).stream()
                .map(post -> {
                    List<PostImage> images = postImageRepository.findByPostIdAndDeletedAtIsNull(post.getId());
                    return PostResponse.from(post, images);
                })
                .toList();
    }

    private static final int MAX_SEARCH_HISTORY = 20;
    private static final DateTimeFormatter CURSOR_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    public PagedPostResponse findAllPaged(String cursor, int limit) {
        List<Post> posts;
        if (cursor == null || cursor.isBlank()) {
            posts = postRepository.findAllFirstPage(PageRequest.of(0, limit + 1));
        } else {
            CursorInfo cursorInfo = parseCursor(cursor);
            posts = postRepository.findAllWithCursor(cursorInfo.createdAt(), cursorInfo.id(),
                    PageRequest.of(0, limit + 1));
        }
        return buildPagedResponse(posts, limit);
    }

    public PagedPostResponse findByBoardIdPaged(Long boardId, String cursor, int limit) {
        List<Post> posts;
        if (cursor == null || cursor.isBlank()) {
            posts = postRepository.findByBoardIdFirstPage(boardId, PageRequest.of(0, limit + 1));
        } else {
            CursorInfo cursorInfo = parseCursor(cursor);
            posts = postRepository.findByBoardIdWithCursor(boardId, cursorInfo.createdAt(), cursorInfo.id(),
                    PageRequest.of(0, limit + 1));
        }
        return buildPagedResponse(posts, limit);
    }

    private static final int HOT_POST_MIN_LIKE_COUNT = 10;

    // Redis 캐시 비활성화 - DB 직접 조회
    public PagedPostResponse findHotPostsPaged(String cursor, int limit) {
        List<Post> posts;
        if (cursor == null || cursor.isBlank()) {
            posts = postRepository.findHotPostsFirstPage(HOT_POST_MIN_LIKE_COUNT, PageRequest.of(0, limit + 1));
        } else {
            CursorInfo cursorInfo = parseCursor(cursor);
            posts = postRepository.findHotPostsWithCursor(HOT_POST_MIN_LIKE_COUNT, cursorInfo.createdAt(),
                    cursorInfo.id(), PageRequest.of(0, limit + 1));
        }
        return buildPagedResponse(posts, limit);
    }

    @Transactional
    public PagedPostResponse searchByKeywordPaged(String keyword, String email, String cursor, int limit) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // 검색 기록 저장 (첫 페이지일 때만)
        if (cursor == null || cursor.isBlank()) {
            searchHistoryRepository.findByMemberIdAndKeywordAndDeletedAtIsNull(member.getId(), keyword)
                    .ifPresentOrElse(
                            existing -> {
                                existing.delete();
                                searchHistoryRepository.save(SearchHistory.builder()
                                        .memberId(member.getId())
                                        .keyword(keyword)
                                        .build());
                            },
                            () -> {
                                searchHistoryRepository.save(SearchHistory.builder()
                                        .memberId(member.getId())
                                        .keyword(keyword)
                                        .build());

                                long count = searchHistoryRepository.countByMemberIdAndDeletedAtIsNull(member.getId());
                                if (count > MAX_SEARCH_HISTORY) {
                                    int deleteCount = (int) (count - MAX_SEARCH_HISTORY);
                                    List<SearchHistory> oldestHistories = searchHistoryRepository
                                            .findOldestByMemberId(member.getId(), PageRequest.of(0, deleteCount));
                                    oldestHistories.forEach(SearchHistory::delete);
                                }
                            });
        }

        List<Post> posts;
        if (cursor == null || cursor.isBlank()) {
            posts = postRepository.searchByKeywordFirstPage(keyword, PageRequest.of(0, limit + 1));
        } else {
            CursorInfo cursorInfo = parseCursor(cursor);
            posts = postRepository.searchByKeywordWithCursor(keyword, cursorInfo.createdAt(), cursorInfo.id(),
                    PageRequest.of(0, limit + 1));
        }
        return buildPagedResponse(posts, limit);
    }

    private PagedPostResponse buildPagedResponse(List<Post> posts, int limit) {
        boolean hasNext = posts.size() > limit;
        List<Post> resultPosts = hasNext ? posts.subList(0, limit) : posts;

        // N+1 방지: 한 번에 모든 이미지 조회
        List<Long> postIds = resultPosts.stream().map(Post::getId).toList();
        Map<Long, List<PostImage>> imageMap = postIds.isEmpty() ? Collections.emptyMap()
                : postImageRepository.findByPostIdInAndDeletedAtIsNull(postIds).stream()
                        .collect(java.util.stream.Collectors.groupingBy(pi -> pi.getPost().getId()));

        List<PostResponse> postResponses = resultPosts.stream()
                .map(post -> {
                    List<PostImage> images = imageMap.getOrDefault(post.getId(), Collections.emptyList());
                    return PostResponse.from(post, images);
                })
                .toList();

        String nextCursor = null;
        if (hasNext && !resultPosts.isEmpty()) {
            Post lastPost = resultPosts.get(resultPosts.size() - 1);
            nextCursor = generateCursor(lastPost.getCreatedAt(), lastPost.getId());
        }

        return new PagedPostResponse(postResponses, nextCursor, hasNext);
    }

    private String generateCursor(LocalDateTime createdAt, Long id) {
        return createdAt.format(CURSOR_FORMATTER) + "_" + id;
    }

    private CursorInfo parseCursor(String cursor) {
        String[] parts = cursor.split("_");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid cursor format");
        }
        LocalDateTime createdAt = LocalDateTime.parse(parts[0], CURSOR_FORMATTER);
        Long id = Long.parseLong(parts[1]);
        return new CursorInfo(createdAt, id);
    }

    private record CursorInfo(LocalDateTime createdAt, Long id) {
    }

    @Transactional
    public List<PostResponse> searchByKeyword(String keyword, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // 검색 기록 저장 (중복 키워드는 시간만 업데이트)
        searchHistoryRepository.findByMemberIdAndKeywordAndDeletedAtIsNull(member.getId(), keyword)
                .ifPresentOrElse(
                        existing -> {
                            // 기존 기록 삭제 후 새로 생성 (최신순 정렬 위해)
                            existing.delete();
                            searchHistoryRepository.save(SearchHistory.builder()
                                    .memberId(member.getId())
                                    .keyword(keyword)
                                    .build());
                        },
                        () -> {
                            searchHistoryRepository.save(SearchHistory.builder()
                                    .memberId(member.getId())
                                    .keyword(keyword)
                                    .build());

                            // 20개 초과 시 오래된 기록 삭제
                            long count = searchHistoryRepository.countByMemberIdAndDeletedAtIsNull(member.getId());
                            if (count > MAX_SEARCH_HISTORY) {
                                int deleteCount = (int) (count - MAX_SEARCH_HISTORY);
                                List<SearchHistory> oldestHistories = searchHistoryRepository
                                        .findOldestByMemberId(member.getId(), PageRequest.of(0, deleteCount));
                                oldestHistories.forEach(SearchHistory::delete);
                            }
                        });

        return postRepository.searchByKeyword(keyword).stream()
                .map(post -> {
                    List<PostImage> images = postImageRepository.findByPostIdAndDeletedAtIsNull(post.getId());
                    return PostResponse.from(post, images);
                })
                .toList();
    }

    public List<SearchHistoryResponse> getSearchHistory(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        return searchHistoryRepository.findByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(member.getId())
                .stream()
                .map(SearchHistoryResponse::from)
                .toList();
    }

    @Transactional
    public void deleteSearchHistory(Long historyId, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        SearchHistory history = searchHistoryRepository.findByIdAndMemberIdAndDeletedAtIsNull(historyId, member.getId())
                .orElseThrow(() -> new IllegalArgumentException("Search history not found"));

        history.delete();
    }

    @Transactional
    public void deleteAllSearchHistory(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        searchHistoryRepository.deleteAllByMemberId(member.getId());
    }

    public List<PopularKeywordResponse> getPopularKeywords() {
        return searchHistoryRepository.findPopularKeywords(PageRequest.of(0, 10))
                .stream()
                .map(row -> new PopularKeywordResponse((String) row[0], (Long) row[1]))
                .toList();
    }

    public PostResponse findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        List<PostImage> images = postImageRepository.findByPostIdAndDeletedAtIsNull(post.getId());
        return PostResponse.from(post, images);
    }

    @Transactional
    public PostDetailResponse findDetailById(Long id, String email) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Member currentMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // 조회수 증가
        post.incrementViewCount();

        Long currentMemberId = currentMember.getId();
        Long postAuthorId = post.getMember() != null ? post.getMember().getId() : null;

        // 이미지 URL 목록
        List<PostImage> images = postImageRepository.findByPostIdAndDeletedAtIsNull(post.getId());
        List<String> imageUrls = images.stream().map(PostImage::getImageUrl).toList();

        // 좋아요 여부
        boolean isLiked = postLikeRepository.existsByPostIdAndMemberId(post.getId(), currentMemberId);

        // 스크랩 여부
        boolean isScraped = scrapRepository.existsByPostIdAndMemberId(post.getId(), currentMemberId);

        // 투표 정보
        PollResponse pollResponse = buildPollResponse(post.getId(), currentMemberId);

        // 댓글 목록
        List<CommentResponse> comments = buildCommentsResponse(post.getId(), currentMemberId, postAuthorId);

        return new PostDetailResponse(
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getId(),
                post.getBoard().getId(),
                postAuthorId != null && postAuthorId.equals(currentMemberId),
                post.getTitle(),
                post.getContent(),
                post.getIsBlinded(),
                imageUrls,
                pollResponse,
                post.getLikeCount(),
                isLiked,
                post.getCommentCount(),
                post.getScrapCount(),
                isScraped,
                comments);
    }

    private PollResponse buildPollResponse(Long postId, Long currentMemberId) {
        Optional<Vote> voteOpt = voteRepository.findByPostIdAndDeletedAtIsNull(postId);
        if (voteOpt.isEmpty()) {
            return null;
        }

        Vote vote = voteOpt.get();
        List<VoteItem> items = voteItemRepository.findByVoteIdAndDeletedAtIsNullOrderByItemOrderAsc(vote.getId());

        // 내가 투표한 옵션
        Long myVotedOptionId = voteRecordRepository
                .findByVoteIdAndMemberIdAndDeletedAtIsNull(vote.getId(), currentMemberId)
                .map(record -> record.getItem().getId())
                .orElse(null);

        // 총 투표 수
        int totalVotes = voteRecordRepository.countByVoteIdAndDeletedAtIsNull(vote.getId());

        // 옵션별 투표 수
        List<PollOptionResponse> options = items.stream()
                .map(item -> {
                    int voteCount = voteRecordRepository.countByItemIdAndDeletedAtIsNull(item.getId());
                    return new PollOptionResponse(item.getId(), item.getContent(), voteCount);
                })
                .toList();

        return new PollResponse(vote.getId(), totalVotes, myVotedOptionId, options);
    }

    private static final String DELETED_COMMENT_MESSAGE = "삭제된 댓글입니다.";

    private List<CommentResponse> buildCommentsResponse(Long postId, Long currentMemberId, Long postAuthorId) {
        // 삭제된 댓글도 포함하여 조회
        List<Comment> parentComments = commentRepository
                .findByPostIdAndParentIdIsNullOrderByCreatedAtAsc(postId);

        // 익명 이름 매핑 (memberId -> 익명번호)
        Map<Long, Integer> anonMap = new HashMap<>();
        int[] anonCounter = { 0 };

        return parentComments.stream()
                .map(comment -> buildCommentResponse(comment, currentMemberId, postAuthorId, anonMap, anonCounter))
                .toList();
    }

    private CommentResponse buildCommentResponse(Comment comment, Long currentMemberId, Long postAuthorId,
            Map<Long, Integer> anonMap, int[] anonCounter) {
        Long commentMemberId = comment.getMember() != null ? comment.getMember().getId() : null;
        boolean isDeleted = comment.getDeletedAt() != null;

        // 좋아요 여부
        boolean isLiked = commentLikeRepository.existsByCommentIdAndMemberId(comment.getId(), currentMemberId);

        // 익명 정보
        AnonResponse anon = buildAnonResponse(commentMemberId, currentMemberId, postAuthorId, anonMap, anonCounter);

        // 대댓글 (삭제된 대댓글도 포함)
        List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(comment.getId());
        List<ReplyResponse> replyResponses = replies.stream()
                .map(reply -> buildReplyResponse(reply, currentMemberId, postAuthorId, anonMap, anonCounter))
                .toList();

        // 삭제된 댓글은 내용을 "삭제된 댓글입니다."로 표시
        String content = isDeleted ? DELETED_COMMENT_MESSAGE : comment.getContent();

        return new CommentResponse(
                comment.getId(),
                comment.getCreatedAt(),
                content,
                comment.getLikeCount(),
                isLiked,
                comment.getIsBlinded(),
                isDeleted,
                anon,
                replyResponses);
    }

    private ReplyResponse buildReplyResponse(Comment reply, Long currentMemberId, Long postAuthorId,
            Map<Long, Integer> anonMap, int[] anonCounter) {
        Long replyMemberId = reply.getMember() != null ? reply.getMember().getId() : null;
        boolean isDeleted = reply.getDeletedAt() != null;

        boolean isLiked = commentLikeRepository.existsByCommentIdAndMemberId(reply.getId(), currentMemberId);
        AnonResponse anon = buildAnonResponse(replyMemberId, currentMemberId, postAuthorId, anonMap, anonCounter);

        // 삭제된 대댓글은 내용을 "삭제된 댓글입니다."로 표시
        String content = isDeleted ? DELETED_COMMENT_MESSAGE : reply.getContent();

        return new ReplyResponse(
                reply.getId(),
                reply.getCreatedAt(),
                content,
                reply.getLikeCount(),
                isLiked,
                reply.getIsBlinded(),
                isDeleted,
                anon);
    }

    private AnonResponse buildAnonResponse(Long memberId, Long currentMemberId, Long postAuthorId,
            Map<Long, Integer> anonMap, int[] anonCounter) {
        if (memberId == null) {
            return new AnonResponse("탈퇴한 사용자", false, false);
        }

        boolean isAuthor = memberId.equals(postAuthorId);
        boolean isMine = memberId.equals(currentMemberId);

        // 익명 번호 할당
        if (!anonMap.containsKey(memberId)) {
            anonMap.put(memberId, ++anonCounter[0]);
        }
        int anonNumber = anonMap.get(memberId);

        String name;
        if (isAuthor) {
            name = "싸용자(작성자)";
        } else {
            name = "싸용자" + anonNumber;
        }

        return new AnonResponse(name, isAuthor, isMine);
    }

    /**
     * 댓글 목록 커서 기반 페이지네이션 조회
     */
    public PagedCommentResponse getCommentsPaged(Long postId, String email, String cursor, int limit) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        Long currentMemberId = member.getId();
        Long postAuthorId = post.getMember() != null ? post.getMember().getId() : null;

        // 부모 댓글 조회 (커서 기반)
        List<Comment> parentComments;
        if (cursor == null || cursor.isBlank()) {
            parentComments = commentRepository.findParentCommentsFirstPage(postId, PageRequest.of(0, limit + 1));
        } else {
            CursorInfo cursorInfo = parseCursor(cursor);
            parentComments = commentRepository.findParentCommentsWithCursor(postId, cursorInfo.createdAt(),
                    cursorInfo.id(),
                    PageRequest.of(0, limit + 1));
        }

        boolean hasNext = parentComments.size() > limit;
        List<Comment> resultComments = hasNext ? parentComments.subList(0, limit) : parentComments;

        // 익명 이름 매핑
        Map<Long, Integer> anonMap = new HashMap<>();
        int[] anonCounter = { 0 };

        List<CommentResponse> commentResponses = resultComments.stream()
                .map(comment -> buildCommentResponse(comment, currentMemberId, postAuthorId, anonMap, anonCounter))
                .toList();

        String nextCursor = null;
        if (hasNext && !resultComments.isEmpty()) {
            Comment lastComment = resultComments.get(resultComments.size() - 1);
            nextCursor = generateCursor(lastComment.getCreatedAt(), lastComment.getId());
        }

        return new PagedCommentResponse(commentResponses, nextCursor, hasNext);
    }

    @Transactional
    public CommentResponse createComment(Long postId, CommentCreateRequest request, String email) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (request.content() == null || request.content().trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }
        // Removed Blocking Censorship Check -> Handled Async by EventListener

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        Comment comment = Comment.builder()
                .post(post)
                .member(member)
                .content(request.content())
                .build();

        Comment savedComment = commentRepository.save(comment);
        post.incrementCommentCount();

        Long postAuthorId = post.getMember() != null ? post.getMember().getId() : null;
        Long currentMemberId = member.getId();

        // 알림 발송: 게시글 작성자에게 (본인 글에 본인이 댓글 달면 제외)
        if (postAuthorId != null && !postAuthorId.equals(currentMemberId)) {
            notificationService.send(postAuthorId, NotificationType.COMMENT,
                    post.getTitle(), "내 글에 새 댓글이 달렸습니다.", "/posts/" + post.getId());

            // FCM Data 메시지 발송 (Android 앱용)
            eventPublisher.publishEvent(new FCMCommentEvent(
                    postAuthorId,
                    FCMDataType.POST_COMMENT,
                    post.getId(),
                    savedComment.getId(),
                    java.time.Instant.now()));
        }

        // 익명 번호 계산 (기존 댓글들 기준)
        Map<Long, Integer> anonMap = new HashMap<>();
        int[] anonCounter = { 0 };
        List<Comment> existingComments = commentRepository
                .findByPostIdAndParentIdIsNullAndDeletedAtIsNullOrderByCreatedAtAsc(postId);
        for (Comment c : existingComments) {
            Long cMemberId = c.getMember() != null ? c.getMember().getId() : null;
            if (cMemberId != null && !anonMap.containsKey(cMemberId)) {
                anonMap.put(cMemberId, ++anonCounter[0]);
            }
            // 대댓글도 확인
            List<Comment> replies = commentRepository.findByParentIdAndDeletedAtIsNullOrderByCreatedAtAsc(c.getId());
            for (Comment r : replies) {
                Long rMemberId = r.getMember() != null ? r.getMember().getId() : null;
                if (rMemberId != null && !anonMap.containsKey(rMemberId)) {
                    anonMap.put(rMemberId, ++anonCounter[0]);
                }
            }
        }

        AnonResponse anon = buildAnonResponse(currentMemberId, currentMemberId, postAuthorId, anonMap, anonCounter);

        // Publish Async Censorship Event
        eventPublisher.publishEvent(new com.ssafy.ssabre.comment.event.CommentCreatedEvent(
                savedComment.getId(), savedComment.getContent()));

        return new CommentResponse(
                savedComment.getId(),
                savedComment.getCreatedAt(),
                savedComment.getContent(),
                0,
                false,
                false,
                false,
                anon,
                Collections.emptyList());
    }

    @Transactional
    public ReplyResponse createReply(Long postId, Long commentId, ReplyCreateRequest request, String email) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (!parentComment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("Comment does not belong to this post");
        }

        if (parentComment.getParentId() != null) {
            throw new IllegalArgumentException("Cannot reply to a reply");
        }

        if (request.content() == null || request.content().trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }
        // Removed Blocking Censorship Check -> Handled Async by EventListener

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        Comment reply = Comment.builder()
                .post(post)
                .member(member)
                .content(request.content())
                .parentId(commentId)
                .build();

        Comment savedReply = commentRepository.save(reply);
        post.incrementCommentCount();

        Long postAuthorId = post.getMember() != null ? post.getMember().getId() : null;
        Long currentMemberId = member.getId();
        Long parentCommentAuthorId = parentComment.getMember() != null ? parentComment.getMember().getId() : null;

        // 알림 발송: 게시글 작성자에게 (본인 제외)
        if (postAuthorId != null && !postAuthorId.equals(currentMemberId)) {
            notificationService.send(postAuthorId, NotificationType.COMMENT,
                    post.getTitle(), "내 글에 새 댓글이 달렸습니다.", "/posts/" + post.getId());

            // FCM Data 메시지 발송 (Android 앱용)
            eventPublisher.publishEvent(new FCMCommentEvent(
                    postAuthorId,
                    FCMDataType.POST_COMMENT,
                    post.getId(),
                    savedReply.getId(),
                    java.time.Instant.now()));
        }

        // 알림 발송: 부모 댓글 작성자에게 (본인 제외, 게시글 작성자와 다른 경우)
        if (parentCommentAuthorId != null && !parentCommentAuthorId.equals(currentMemberId)
                && !parentCommentAuthorId.equals(postAuthorId)) {
            notificationService.send(parentCommentAuthorId, NotificationType.REPLY,
                    post.getTitle(), "내 댓글에 답글이 달렸습니다.", "/posts/" + post.getId());

            // FCM Data 메시지 발송 (Android 앱용)
            eventPublisher.publishEvent(new FCMCommentEvent(
                    parentCommentAuthorId,
                    FCMDataType.COMMENT_REPLY,
                    post.getId(),
                    savedReply.getId(),
                    java.time.Instant.now()));
        }

        // 익명 번호 계산
        Map<Long, Integer> anonMap = new HashMap<>();
        int[] anonCounter = { 0 };
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

        AnonResponse anon = buildAnonResponse(currentMemberId, currentMemberId, postAuthorId, anonMap, anonCounter);

        // Publish Async Censorship Event
        eventPublisher.publishEvent(new com.ssafy.ssabre.comment.event.CommentCreatedEvent(
                savedReply.getId(), savedReply.getContent()));

        return new ReplyResponse(
                savedReply.getId(),
                savedReply.getCreatedAt(),
                savedReply.getContent(),
                0,
                false,
                false,
                false,
                anon);
    }

    @Transactional
    public PostResponse update(Long id, PostUpdateRequest request, String email) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (post.getMember() == null || !post.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        // AI Censorship (Batching)
        String combinedContent = "Title: " + request.title() + "\nContent: " + request.content();
        boolean isSafe = aiCensorshipService.isContentSafe(combinedContent);
        boolean isBlinded = !isSafe;
        if (isBlinded) {
            log.warn("Post update content detected as unsafe. Setting blinded. Post ID: {}", id);
        }

        post.update(request.title(), request.content());
        post.setBlinded(isBlinded); // Blind 여부 업데이트

        List<PostImage> images = postImageRepository.findByPostIdAndDeletedAtIsNull(post.getId());
        return PostResponse.from(post, images);
    }

    @Transactional
    public void delete(Long id, String email) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (post.getMember() == null || !post.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        try {
            uploadService.deleteFolder("post/" + id);
        } catch (IOException e) {
            log.error("Failed to delete images for post {}", id, e);
        }

        postImageRepository.deleteByPostId(id);
        post.delete();
    }

    @Transactional
    public PollResponse vote(Long postId, VoteRequest request, String email) {
        if (!postRepository.existsByIdAndDeletedAtIsNull(postId)) {
            throw new IllegalArgumentException("Post not found");
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        Vote vote = voteRepository.findByPostIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new IllegalArgumentException("Vote not found for this post"));

        VoteItem selectedItem = voteItemRepository.findByIdAndDeletedAtIsNull(request.optionId())
                .orElseThrow(() -> new IllegalArgumentException("Vote option not found"));

        if (!selectedItem.getVote().getId().equals(vote.getId())) {
            throw new IllegalArgumentException("Vote option does not belong to this poll");
        }

        // 기존 투표 확인
        Optional<VoteRecord> existingRecord = voteRecordRepository.findByVoteIdAndMemberIdAndDeletedAtIsNull(
                vote.getId(), member.getId());

        if (existingRecord.isPresent()) {
            // 이미 투표한 경우 - 선택 변경
            existingRecord.get().updateItem(selectedItem);
        } else {
            // 새로운 투표
            VoteRecord voteRecord = VoteRecord.builder()
                    .vote(vote)
                    .member(member)
                    .item(selectedItem)
                    .build();
            voteRecordRepository.save(voteRecord);
        }

        return buildPollResponse(postId, member.getId());
    }

    @Transactional
    public PostLikeResponse like(Long postId, String email) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        boolean alreadyLiked = postLikeRepository.existsByPostIdAndMemberId(postId, member.getId());

        if (!alreadyLiked) {
            PostLike postLike = new PostLike(postId, member.getId());
            postLikeRepository.save(postLike);
            post.incrementLikeCount();
        }

        return new PostLikeResponse(true, post.getLikeCount());
    }

    @Transactional
    public PostLikeResponse unlike(Long postId, String email) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        boolean alreadyLiked = postLikeRepository.existsByPostIdAndMemberId(postId, member.getId());

        if (alreadyLiked) {
            postLikeRepository.deleteByPostIdAndMemberId(postId, member.getId());
            post.decrementLikeCount();
        }

        return new PostLikeResponse(false, post.getLikeCount());
    }

    @Transactional
    public ScrapResponse scrap(Long postId, String email) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        boolean alreadyScrapped = scrapRepository.existsByPostIdAndMemberId(postId, member.getId());

        if (!alreadyScrapped) {
            Scrap scrap = new Scrap(member.getId(), postId);
            scrapRepository.save(scrap);
            post.incrementScrapCount();
        }

        return new ScrapResponse(true);
    }

    @Transactional
    public ScrapResponse unscrap(Long postId, String email) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        boolean alreadyScrapped = scrapRepository.existsByPostIdAndMemberId(postId, member.getId());

        if (alreadyScrapped) {
            scrapRepository.deleteByPostIdAndMemberId(postId, member.getId());
            post.decrementScrapCount();
        }

        return new ScrapResponse(true);
    }
}
