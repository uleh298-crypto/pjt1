package com.ssafy.ssabre.chat.service;

import com.ssafy.ssabre.chat.dto.ChatListUpdateResponse;
import com.ssafy.ssabre.chat.dto.ChatMessageResponse;
import com.ssafy.ssabre.chat.dto.ChatMessageSendRequest;
import com.ssafy.ssabre.chat.dto.ChatRoomCreateRequest;
import com.ssafy.ssabre.chat.dto.ChatRoomResponse;
import com.ssafy.ssabre.chat.entity.ChatMessage;
import com.ssafy.ssabre.chat.entity.ChatRoom;
import com.ssafy.ssabre.chat.entity.ChatRoomMember;
import com.ssafy.ssabre.chat.repository.ChatMessageRepository;
import com.ssafy.ssabre.chat.repository.ChatRoomMemberRepository;
import com.ssafy.ssabre.chat.repository.ChatRoomRepository;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.MemberRepository;
import com.ssafy.ssabre.notification.entity.NotificationType;
import com.ssafy.ssabre.notification.event.FCMChatEvent;
import com.ssafy.ssabre.notification.service.NotificationService;
import com.ssafy.ssabre.post.entity.Post;
import com.ssafy.ssabre.post.entity.PostAnonymousNumber;
import com.ssafy.ssabre.post.repository.PostAnonymousNumberRepository;
import com.ssafy.ssabre.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PostAnonymousNumberRepository postAnonymousNumberRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 1대1 채팅방 생성
     */
    @Transactional
    public Long createChatRoom(ChatRoomCreateRequest request, String email) {
        Member creator = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // 게시글에서 작성자(target) 조회
        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Member target = post.getMember();

        if (target == null) {
            throw new IllegalArgumentException("탈퇴한 사용자와 채팅방을 생성할 수 없습니다.");
        }

        if (creator.getId().equals(target.getId())) {
            throw new IllegalArgumentException("자기 자신과 채팅방을 생성할 수 없습니다.");
        }

        // 같은 게시글에서 동일한 두 사용자의 채팅방이 이미 존재하는지 확인
        var existingRoomOpt = chatRoomRepository.findByPostIdAndMembers(request.postId(), creator.getId(), target.getId());
        if (existingRoomOpt.isPresent()) {
            ChatRoom existingRoom = existingRoomOpt.get();
            ChatRoomMember creatorMember = chatRoomMemberRepository
                    .findByChatRoomIdAndMemberId(existingRoom.getId(), creator.getId())
                    .orElseThrow(() -> new IllegalArgumentException("채팅방 멤버 정보를 찾을 수 없습니다."));

            if (creatorMember.isDeleted()) {
                // 요청자가 이전에 나간 방 → 재활성화
                creatorMember.reactivate();
                return existingRoom.getId();
            }
            throw new IllegalArgumentException("이미 해당 게시글에서 생성된 채팅방이 존재합니다.");
        }

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .post(post)
                .build();
        chatRoomRepository.save(chatRoom);

        // 생성자 추가
        chatRoomMemberRepository.save(ChatRoomMember.builder()
                .chatRoom(chatRoom)
                .member(creator)
                .build());

        // 상대방 추가
        chatRoomMemberRepository.save(ChatRoomMember.builder()
                .chatRoom(chatRoom)
                .member(target)
                .build());

        return chatRoom.getId();
    }

    /**
     * 내 채팅방 목록 조회
     */
    @Transactional
    public List<ChatRoomResponse> getMyChatRooms(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        List<ChatRoom> chatRooms = chatRoomRepository.findByMemberId(member.getId());

        return chatRooms.stream()
                .map(chatRoom -> {
                    // 마지막 메시지 조회
                    ChatMessage lastMessage = chatMessageRepository
                            .findTopByChatRoomIdOrderBySentAtDesc(chatRoom.getId())
                            .orElse(null);

                    // 상대방 조회
                    Member opponent = findOpponent(chatRoom.getId(), member.getId());
                    String opponentName = generateAnonymousName(opponent, chatRoom.getPost());
                    String chatRoomName = generateChatRoomName(chatRoom.getPost(), opponentName);

                    return new ChatRoomResponse(
                            chatRoom.getId(),
                            chatRoomName,
                            opponentName,
                            chatRoom.getPost() != null ? chatRoom.getPost().getId() : null,
                            chatRoom.getPost() != null ? chatRoom.getPost().getTitle() : null,
                            lastMessage != null ? lastMessage.getContent() : null,
                            lastMessage != null ? lastMessage.getSentAt() : null,
                            false, // 목록에 나오는 방은 삭제되지 않은 상태
                            chatRoom.getCreatedAt()
                    );
                })
                .toList();
    }

    /**
     * 메시지 전송
     */
    @Transactional
    public Long sendMessage(Long chatRoomId, ChatMessageSendRequest request, String email) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found"));

        Member sender = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // 멤버가 해당 방에 속해있는지 검증 + 나간 사용자 체크
        ChatRoomMember senderMember = chatRoomMemberRepository.findByChatRoomIdAndMemberId(chatRoomId, sender.getId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방에 참여하지 않은 사용자입니다."));

        if (senderMember.isDeleted()) {
            throw new IllegalArgumentException("나간 채팅방에는 메시지를 보낼 수 없습니다.");
        }

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(request.content())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 발신자 익명 이름 생성
        String senderName = generateAnonymousName(sender, chatRoom.getPost());

        // WebSocket 브로드캐스트: 채팅방 목록 업데이트
        ChatListUpdateResponse listUpdate = new ChatListUpdateResponse(
                chatRoomId,
                request.content(),
                savedMessage.getSentAt()
        );

        // 알림 발송: 상대방에게
        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoomId(chatRoomId);
        String chatRoomName = generateChatRoomName(chatRoom.getPost(), senderName);

        for (ChatRoomMember member : members) {
            // 나간 멤버에게는 WebSocket/알림 전송하지 않음
            if (member.isDeleted()) {
                continue;
            }

            Long memberId = member.getMember().getId();
            boolean isMine = memberId.equals(sender.getId());

            // WebSocket 메시지 전송 (각 멤버에게 개별 전송, isMine 계산)
            ChatMessageResponse messageResponse = new ChatMessageResponse(
                    savedMessage.getId(),
                    isMine,
                    senderName,
                    request.content(),
                    savedMessage.getSentAt()
            );
            messagingTemplate.convertAndSend("/topic/user/" + memberId + "/chat/" + chatRoomId, messageResponse);

            // 채팅방 목록 업데이트 (모든 멤버에게)
            messagingTemplate.convertAndSend("/topic/user/" + memberId + "/chat-list", listUpdate);

            // 푸시 알림 (상대방에게만)
            if (!member.getMember().getId().equals(sender.getId())) {
                // 기존 Notification 알림 (DB 저장 + 기본 FCM)
                notificationService.send(member.getMember(), NotificationType.MESSAGE,
                        chatRoomName, request.content(), "/chats/" + chatRoomId);

                // FCM Data 메시지 발송 (트랜잭션 커밋 후)
                String preview = request.content().length() > 50
                        ? request.content().substring(0, 50) + "..."
                        : request.content();
                eventPublisher.publishEvent(new FCMChatEvent(
                        member.getMember().getId(),
                        chatRoomId,
                        Instant.now(),  // UTC 기준
                        preview,
                        senderName
                ));
            }
        }

        return savedMessage.getId();
    }

    /**
     * 채팅 메시지 목록 조회
     */
    @Transactional
    public List<ChatMessageResponse> getChatMessages(Long chatRoomId, String email) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found"));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // 멤버가 해당 방에 속해있는지 검증 + 삭제 여부 체크
        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByChatRoomIdAndMemberId(chatRoomId, member.getId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방에 참여하지 않은 사용자입니다."));

        if (chatRoomMember.isDeleted()) {
            throw new IllegalArgumentException("삭제된 채팅방입니다.");
        }

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId);
        Post post = chatRoom.getPost();
        Long currentMemberId = member.getId();

        return messages.stream()
                .map(msg -> new ChatMessageResponse(
                        msg.getId(),
                        msg.getSender() != null && msg.getSender().getId().equals(currentMemberId),
                        msg.getSender() != null ? generateAnonymousName(msg.getSender(), post) : "탈퇴한 사용자",
                        msg.getContent(),
                        msg.getSentAt()
                ))
                .toList();
    }

    /**
     * 채팅방 나가기 (사용자별 soft delete)
     */
    @Transactional
    public void exitChatRoom(Long roomId, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found"));

        // 멤버가 해당 방에 속해있는지 검증
        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByChatRoomIdAndMemberId(roomId, member.getId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방에 참여하지 않은 사용자입니다."));

        if (chatRoomMember.isDeleted()) {
            throw new IllegalArgumentException("이미 나간 채팅방입니다.");
        }

        // 해당 사용자만 soft delete
        chatRoomMember.delete();
    }

    /**
     * 채팅방 멤버 ID 목록 조회 (WebSocket 브로드캐스트용)
     */
    public List<Long> getChatRoomMemberIds(Long roomId) {
        return chatRoomMemberRepository.findByChatRoomId(roomId).stream()
                .map(member -> member.getMember().getId())
                .toList();
    }

    /**
     * 채팅방 상세 조회
     */
    @Transactional
    public ChatRoomResponse getChatRoom(Long roomId, String email) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found"));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // 멤버가 해당 방에 속해있는지 검증 + 삭제 여부 체크
        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByChatRoomIdAndMemberId(roomId, member.getId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방에 참여하지 않은 사용자입니다."));

        if (chatRoomMember.isDeleted()) {
            throw new IllegalArgumentException("삭제된 채팅방입니다.");
        }

        ChatMessage lastMessage = chatMessageRepository
                .findTopByChatRoomIdOrderBySentAtDesc(chatRoom.getId())
                .orElse(null);

        // 상대방 조회
        Member opponent = findOpponent(roomId, member.getId());
        String opponentName = generateAnonymousName(opponent, chatRoom.getPost());
        String chatRoomName = generateChatRoomName(chatRoom.getPost(), opponentName);

        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoomName,
                opponentName,
                chatRoom.getPost() != null ? chatRoom.getPost().getId() : null,
                chatRoom.getPost() != null ? chatRoom.getPost().getTitle() : null,
                lastMessage != null ? lastMessage.getContent() : null,
                lastMessage != null ? lastMessage.getSentAt() : null,
                false, // 접근 가능한 방은 삭제되지 않은 상태
                chatRoom.getCreatedAt()
        );
    }

    /**
     * 채팅방에서 상대방 찾기
     */
    private Member findOpponent(Long chatRoomId, Long myMemberId) {
        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoomId(chatRoomId);
        return members.stream()
                .map(ChatRoomMember::getMember)
                .filter(m -> !m.getId().equals(myMemberId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 익명 이름 생성
     * - 게시글 작성자: "작성자"
     * - 그 외: "싸용자" + (게시글별 순번)
     */
    private String generateAnonymousName(Member member, Post post) {
        if (member == null) {
            return "알 수 없음";
        }

        if (post == null) {
            return "싸용자";
        }

        // 게시글 작성자인 경우
        if (post.getMember() != null && post.getMember().getId().equals(member.getId())) {
            return "작성자";
        }

        // 게시글별 익명 번호 조회 또는 생성
        Integer anonymousNumber = getOrCreateAnonymousNumber(post, member);
        return "싸용자" + anonymousNumber;
    }

    /**
     * 게시글별 익명 번호 조회 또는 생성
     */
    private Integer getOrCreateAnonymousNumber(Post post, Member member) {
        return postAnonymousNumberRepository.findByPostIdAndMemberId(post.getId(), member.getId())
                .map(PostAnonymousNumber::getAnonymousNumber)
                .orElseGet(() -> {
                    // 새 익명 번호 부여 (현재 최대값 + 1)
                    Integer maxNumber = postAnonymousNumberRepository.findMaxAnonymousNumberByPostId(post.getId());
                    Integer newNumber = maxNumber + 1;

                    PostAnonymousNumber newAnonymous = PostAnonymousNumber.builder()
                            .post(post)
                            .member(member)
                            .anonymousNumber(newNumber)
                            .build();
                    postAnonymousNumberRepository.save(newAnonymous);

                    return newNumber;
                });
    }

    /**
     * 채팅방 이름 생성
     * - 게시글 있음: "게시글제목의 익명이름"
     * - 게시글 없음: "익명이름"
     */
    private String generateChatRoomName(Post post, String opponentName) {
        if (post != null && post.getTitle() != null) {
            return post.getTitle() + "의 " + opponentName;
        }
        return opponentName;
    }
}
