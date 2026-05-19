package com.ssafy.ssabre.member.service;

import com.ssafy.ssabre.auth.service.MattermostAuthService;
import com.ssafy.ssabre.campus.entity.Campus;
import com.ssafy.ssabre.campus.entity.Classes;
import com.ssafy.ssabre.campus.repository.ClassesRepository;
import com.ssafy.ssabre.campus.repository.CampusRepository;
import com.ssafy.ssabre.campus.repository.EnrollmentRepository;
import com.ssafy.ssabre.campus.entity.Enrollment;
import com.ssafy.ssabre.chat.entity.ChatRoom;
import com.ssafy.ssabre.chat.entity.ChatRoomMember;
import com.ssafy.ssabre.chat.repository.ChatMessageRepository;
import com.ssafy.ssabre.chat.repository.ChatRoomMemberRepository;
import com.ssafy.ssabre.chat.repository.ChatRoomRepository;
import com.ssafy.ssabre.comment.repository.CommentLikeRepository;
import com.ssafy.ssabre.comment.repository.CommentRepository;
import com.ssafy.ssabre.comment.entity.Comment;
import com.ssafy.ssabre.global.error.GlobalErrorCode;
import com.ssafy.ssabre.global.error.exception.BusinessException;
import com.ssafy.ssabre.inquiry.repository.InquiryRepository;
import com.ssafy.ssabre.member.dto.MemberResponse;
import com.ssafy.ssabre.member.dto.MemberSignUpRequest;
import com.ssafy.ssabre.member.dto.MyCommentResponse;
import com.ssafy.ssabre.member.dto.MyPageCounts;
import com.ssafy.ssabre.member.dto.MyPagePortfolioSummary;
import com.ssafy.ssabre.member.dto.MyPageResponse;
import com.ssafy.ssabre.member.dto.MyPageUserInfo;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.BlockedMemberRepository;
import com.ssafy.ssabre.member.repository.DDayRepository;
import com.ssafy.ssabre.member.repository.MemberRepository;
import com.ssafy.ssabre.member.repository.MemberSettingsRepository;
import com.ssafy.ssabre.notification.repository.NotificationRepository;
import com.ssafy.ssabre.notification.repository.NotificationSettingRepository;
import com.ssafy.ssabre.portfolio.entity.Portfolio;
import com.ssafy.ssabre.portfolio.entity.PortfolioStack;
import com.ssafy.ssabre.portfolio.entity.PortfolioUrl;
import com.ssafy.ssabre.portfolio.repository.PortfolioImageRepository;
import com.ssafy.ssabre.portfolio.repository.PortfolioRepository;
import com.ssafy.ssabre.portfolio.repository.PortfolioStackRepository;
import com.ssafy.ssabre.portfolio.repository.PortfolioUrlRepository;
import com.ssafy.ssabre.post.dto.PostResponse;
import com.ssafy.ssabre.post.entity.Post;
import com.ssafy.ssabre.post.entity.PostImage;
import com.ssafy.ssabre.post.entity.Scrap;
import com.ssafy.ssabre.post.repository.PostAnonymousNumberRepository;
import com.ssafy.ssabre.post.repository.PostImageRepository;
import com.ssafy.ssabre.post.repository.PostLikeRepository;
import com.ssafy.ssabre.post.repository.PostRepository;
import com.ssafy.ssabre.post.repository.ScrapRepository;
import com.ssafy.ssabre.post.repository.SearchHistoryRepository;
import com.ssafy.ssabre.post.repository.VoteRecordRepository;
import com.ssafy.ssabre.project.repository.ProjectRepository;
import com.ssafy.ssabre.report.repository.ReportRepository;
import com.ssafy.ssabre.study.entity.Study;
import com.ssafy.ssabre.study.repository.StudyApplicationRepository;
import com.ssafy.ssabre.study.repository.StudyMemberRepository;
import com.ssafy.ssabre.study.repository.StudyRepository;
import com.ssafy.ssabre.study.repository.StudyTaskRepository;
import com.ssafy.ssabre.team.entity.TeamTask;
import com.ssafy.ssabre.study.entity.StudyTask;
import com.ssafy.ssabre.team.entity.Team;
import com.ssafy.ssabre.team.repository.TeamApplicationRepository;
import com.ssafy.ssabre.team.repository.TeamMemberRepository;
import com.ssafy.ssabre.team.repository.TeamRepository;
import com.ssafy.ssabre.team.repository.TeamTaskRepository;
import com.ssafy.ssabre.upload.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MattermostAuthService mattermostAuthService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ScrapRepository scrapRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CampusRepository campusRepository;
    private final ClassesRepository classesRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioStackRepository portfolioStackRepository;
    private final PortfolioUrlRepository portfolioUrlRepository;
    private final PostImageRepository postImageRepository;
    private final UploadService uploadService;
    private final TeamMemberRepository teamMemberRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final VoteRecordRepository voteRecordRepository;
    private final PostAnonymousNumberRepository postAnonymousNumberRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final BlockedMemberRepository blockedMemberRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final DDayRepository dDayRepository;
    private final MemberSettingsRepository memberSettingsRepository;
    private final ReportRepository reportRepository;
    private final InquiryRepository inquiryRepository;
    private final ProjectRepository projectRepository;
    private final PortfolioImageRepository portfolioImageRepository;
    private final TeamTaskRepository teamTaskRepository;
    private final StudyTaskRepository studyTaskRepository;
    private final TeamRepository teamRepository;
    private final StudyRepository studyRepository;
    private final TeamApplicationRepository teamApplicationRepository;
    private final StudyApplicationRepository studyApplicationRepository;

    @Transactional
    public boolean signUp(MemberSignUpRequest request) {

        // Mattermost 인증 여부 확인
        if (!mattermostAuthService.isVerified(request.mattermostId())) {
            throw new IllegalArgumentException("Mattermost 인증이 완료되지 않았습니다. 인증을 먼저 진행해주세요.");
        }

        // Password encoding
        String encodedPassword = passwordEncoder.encode(request.password());
        java.util.Optional<Member> emailOwner = memberRepository.findByEmail(request.email());
        Member memberToSave;

        if (emailOwner.isPresent()) {
            Member owner = emailOwner.get();
            if (owner.getDeletedAt() == null) {
                // Active user exists
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }

            // Mattermost ID check (Strict: different active user)
            memberRepository.findByMattermostIdAndDeletedAtIsNull(request.mattermostId())
                    .filter(m -> !m.getId().equals(owner.getId()))
                    .ifPresent(m -> {
                        throw new IllegalArgumentException("이미 등록된 Mattermost ID입니다.");
                    });

            owner.reactivate(
                    encodedPassword,
                    request.name(),
                    request.studentNo(),
                    request.mattermostId(),
                    null);
            memberToSave = owner;
        } else {
            // New User Case
            // Check Mattermost ID
            if (memberRepository.findByMattermostIdAndDeletedAtIsNull(request.mattermostId()).isPresent()) {
                throw new IllegalArgumentException("이미 등록된 Mattermost ID입니다.");
            }

            // StudentNo duplicate check (Strict: includes deleted members)
            if (memberRepository.existsByStudentNoAndDeletedAtIsNull(request.studentNo())) {
                throw new IllegalArgumentException("이미 등록된 학번입니다.");
            }

            memberToSave = request.toEntity(encodedPassword);
        }

        // 회원 저장
        Member savedMember = memberRepository.save(memberToSave);

        // Enrollment 생성 (campus, generation, classNo가 있는 경우)
        if (request.campus() != null && request.generation() != null && request.classNo() != null) {
            Classes classes = classesRepository.findByCampusIdAndGenerationAndClassNoAndDeletedAtIsNull(
                    request.campus(), request.generation(), request.classNo())
                    .orElseThrow(() -> new IllegalArgumentException("해당 반 정보를 찾을 수 없습니다."));

            Enrollment enrollment = Enrollment.builder()
                    .member(savedMember)
                    .classes(classes)
                    .build();
            enrollmentRepository.save(enrollment);
        }

        return true;
    }

    public java.util.List<Member> findAll() {
        return memberRepository.findAll();
    }

    public java.util.Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    public java.util.Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmailAndDeletedAtIsNull(email);
    }

    public MemberResponse getMemberInfo(String email) {
        Member member = memberRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        return getMemberInfo(member);
    }

    public MemberResponse getMemberInfo(Member member) {
        String campusName = null;
        Integer generation = null;
        Integer classNo = null;

        Enrollment enrollment = enrollmentRepository.findByMember_IdAndDeletedAtIsNull(member.getId()).orElse(null);
        if (enrollment != null) {
            Classes classes = enrollment.getClasses();
            generation = classes.getGeneration();
            classNo = classes.getClassNo();
            Campus campus = campusRepository.findById(classes.getCampus().getId()).orElse(null);
            if (campus != null) {
                campusName = campus.getName();
            }
        }

        return MemberResponse.from(member, campusName, generation, classNo);
    }

    public boolean checkEmailDuplicate(String email) {
        return memberRepository.findByEmailAndDeletedAtIsNull(email).isPresent();
    }

    @Transactional
    public Member update(String email, com.ssafy.ssabre.member.dto.MemberUpdateRequest request) {
        Member member = memberRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        String profileImageUrl = request.profileImageUrl();

        // 프로필 이미지가 있으면 temp에서 profile/{memberId}/로 이동
        if (profileImageUrl != null && profileImageUrl.contains("/temp/")) {
            try {
                // 기존 프로필 이미지 폴더 삭제
                uploadService.deleteFolder("profile/" + member.getId());

                String targetFolder = "profile/" + member.getId();
                List<String> movedUrls = uploadService.moveFromTemp(List.of(profileImageUrl), targetFolder);
                profileImageUrl = movedUrls.isEmpty() ? null : movedUrls.get(0);
            } catch (IOException e) {
                log.error("Failed to move profile image for member {}", member.getId(), e);
            }
        }

        member.update(profileImageUrl);
        return member;
    }

    @Transactional
    public void delete(String email) {
        Member member = memberRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        Long memberId = member.getId();

        // 1. 활성 팀/스터디 소속 검증
        if (!teamMemberRepository.findActiveMembershipsByMemberId(memberId).isEmpty()
                || !studyMemberRepository.findActiveMembershipsByMemberId(memberId).isEmpty()) {
            throw new BusinessException(GlobalErrorCode.MEMBER_IN_GROUP);
        }

        // 2. 글/댓글 soft delete + member 연관 해제 (이미 삭제된 것 포함)
        List<Post> posts = postRepository.findAllByMemberId(memberId);
        for (Post post : posts) {
            if (post.getDeletedAt() == null) {
                post.delete();
            }
            post.clearMember();
        }
        List<Comment> comments = commentRepository.findAllByMemberId(memberId);
        for (Comment comment : comments) {
            if (comment.getDeletedAt() == null) {
                comment.delete();
            }
            comment.clearMember();
        }

        // 3. 상호작용 데이터 hard delete
        postLikeRepository.deleteByMemberId(memberId);
        commentLikeRepository.deleteByMemberId(memberId);
        scrapRepository.deleteByMemberId(memberId);
        voteRecordRepository.deleteByMemberId(memberId);
        postAnonymousNumberRepository.deleteByMemberId(memberId);
        searchHistoryRepository.hardDeleteAllByMemberId(memberId);

        // 4. 채팅 관련 hard delete
        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findByMemberId(memberId);
        for (ChatRoomMember crm : chatRoomMembers) {
            ChatRoom chatRoom = crm.getChatRoom();
            chatMessageRepository.deleteByChatRoomId(chatRoom.getId());
            chatRoomMemberRepository.deleteByChatRoomId(chatRoom.getId());
            chatRoomRepository.delete(chatRoom);
        }

        // 5. 포트폴리오/프로젝트 hard delete
        List<Portfolio> portfolios = portfolioRepository.findByMemberId(memberId);
        for (Portfolio portfolio : portfolios) {
            Long portfolioId = portfolio.getId();
            teamApplicationRepository.deleteByPortfolioId(portfolioId);
            studyApplicationRepository.deleteByPortfolioId(portfolioId);
            projectRepository.deleteByPortfolioId(portfolioId);
            portfolioImageRepository.deleteByPortfolioId(portfolioId);
            portfolioStackRepository.deleteByPortfolioId(portfolioId);
            portfolioUrlRepository.deleteByPortfolioId(portfolioId);
        }
        portfolioRepository.deleteAll(portfolios);

        // 6. TeamTask/StudyTask의 creator 연관 해제
        List<TeamTask> teamTasks = teamTaskRepository.findByCreatorId(memberId);
        for (TeamTask task : teamTasks) {
            task.clearCreator();
        }
        List<StudyTask> studyTasks = studyTaskRepository.findByCreatorId(memberId);
        for (StudyTask task : studyTasks) {
            task.clearCreator();
        }

        // 7. 팀/스터디 멤버십 기록 삭제 (비활성 기록)
        teamMemberRepository.deleteByMemberId(memberId);
        studyMemberRepository.deleteByMemberId(memberId);

        // 8. 기타 회원 관련 데이터 hard delete
        blockedMemberRepository.deleteByBlockerIdOrBlockedId(memberId, memberId);
        reportRepository.deleteByReporterId(memberId);
        inquiryRepository.deleteByMemberId(memberId);
        notificationRepository.deleteByMemberId(memberId);
        notificationSettingRepository.deleteByMemberId(memberId);
        dDayRepository.deleteByMemberId(memberId);
        enrollmentRepository.deleteByMemberId(memberId);
        memberSettingsRepository.deleteByMemberId(memberId);

        // 9. 팀/스터디 leader 연관 해제 (soft delete된 팀/스터디 포함)
        List<Team> leaderTeams = teamRepository.findByLeaderId(memberId);
        for (Team team : leaderTeams) {
            team.clearLeader();
        }
        List<Study> leaderStudies = studyRepository.findByLeaderId(memberId);
        for (Study study : leaderStudies) {
            study.clearLeader();
        }

        // 10. 프로필 이미지 폴더 삭제
        try {
            uploadService.deleteFolder("profile/" + memberId);
        } catch (IOException e) {
            log.error("Failed to delete profile image for member {}", memberId, e);
        }

        // 11. 회원 hard delete
        memberRepository.delete(member);
    }

    public MyPageResponse getMyPage(String email) {
        Member member = memberRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // 1. User 정보 조회
        String campusName = null;
        Integer generation = null;
        Enrollment enrollment = enrollmentRepository.findByMember_IdAndDeletedAtIsNull(member.getId()).orElse(null);
        if (enrollment != null) {
            Classes classes = enrollment.getClasses();
            generation = classes.getGeneration();
            Campus campus = campusRepository.findById(classes.getCampus().getId()).orElse(null);
            if (campus != null) {
                campusName = campus.getName();
            }
        }
        MyPageUserInfo userInfo = new MyPageUserInfo(
                member.getId(),
                member.getName(),
                member.getMattermostId(),
                campusName,
                generation,
                member.getProfileImageUrl());

        // 2. Counts 정보 조회
        long postCount = postRepository.countByMember_IdAndDeletedAtIsNull(member.getId());
        long commentCount = commentRepository.countByMember_IdAndDeletedAtIsNull(member.getId());
        long scrapCount = scrapRepository.countByMemberId(member.getId());
        MyPageCounts counts = new MyPageCounts(postCount, commentCount, scrapCount);

        // 3. Portfolio Summary 정보 조회
        List<Portfolio> portfolios = portfolioRepository.findByMemberId(member.getId());
        Map<String, String> techStack = new HashMap<>();
        String ssafySwRating = null;
        String solvedAcRank = null;
        List<String> links = new java.util.ArrayList<>();
        List<String> projects = portfolios.stream()
                .map(Portfolio::getTitle)
                .filter(title -> title != null && !title.isEmpty())
                .collect(Collectors.toList());

        if (!portfolios.isEmpty()) {
            Portfolio mainPortfolio = portfolios.get(0);
            ssafySwRating = mainPortfolio.getSwTestRank();
            solvedAcRank = mainPortfolio.getSolvedacRank();

            // 기술 스택 조회 (N+1 방지: Stack을 Fetch Join으로 조회)
            List<PortfolioStack> stacks = portfolioStackRepository.findAllByPortfolioId(mainPortfolio.getId());
            for (PortfolioStack ps : stacks) {
                if (ps.getStack() != null) {
                    String level = ps.getExpertLevel();
                    String koreanLevel = switch (level) {
                        case "high" -> "상";
                        case "mid" -> "중";
                        case "low" -> "하";
                        default -> level;
                    };
                    techStack.put(ps.getStack().getName(), koreanLevel);
                }
            }

            // 링크 조회
            List<PortfolioUrl> urls = portfolioUrlRepository.findAllByPortfolioId(mainPortfolio.getId());
            for (PortfolioUrl pu : urls) {
                if (pu.getDeletedAt() == null) {
                    links.add(pu.getUrl());
                }
            }
        }

        MyPagePortfolioSummary portfolioSummary = new MyPagePortfolioSummary(
                techStack,
                ssafySwRating,
                solvedAcRank,
                links,
                projects);

        return new MyPageResponse(userInfo, counts, portfolioSummary);
    }

    public List<PostResponse> getMyPosts(String email) {
        Member member = memberRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        List<Post> posts = postRepository.findByMember_IdAndDeletedAtIsNullOrderByCreatedAtDesc(member.getId());

        return posts.stream()
                .map(post -> {
                    List<PostImage> images = postImageRepository.findByPostIdAndDeletedAtIsNull(post.getId());
                    return PostResponse.from(post, images, member.getId());
                })
                .toList();
    }

    public List<MyCommentResponse> getMyComments(String email) {
        Member member = memberRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        List<Comment> comments = commentRepository
                .findByMember_IdAndDeletedAtIsNullOrderByCreatedAtDesc(member.getId());

        return comments.stream()
                .map(comment -> {
                    Post post = comment.getPost();
                    return new MyCommentResponse(
                            comment.getId(),
                            comment.getContent(),
                            comment.getCreatedAt(),
                            comment.getParentId() != null,
                            post.getId(),
                            post.getTitle(),
                            post.getBoard().getId(),
                            post.getBoard().getName());
                })
                .toList();
    }

    public List<PostResponse> getMyScraps(String email) {
        Member member = memberRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        List<Scrap> scraps = scrapRepository.findByMemberIdOrderByCreatedAtDesc(member.getId());

        return scraps.stream()
                .filter(scrap -> scrap.getPost().getDeletedAt() == null)
                .map(scrap -> {
                    Post post = scrap.getPost();
                    List<PostImage> images = postImageRepository.findByPostIdAndDeletedAtIsNull(post.getId());
                    return PostResponse.from(post, images, member.getId());
                })
                .toList();
    }
}