package com.ssafy.ssabre.comment.event;

import com.ssafy.ssabre.comment.service.CommentService;
import com.ssafy.ssabre.global.service.AiCensorshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentEventListener {

    private final AiCensorshipService aiCensorshipService;
    private final CommentService commentService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreatedEvent(CommentCreatedEvent event) {
        checkAndBlindComment(event.getCommentId(), event.getContent(), "CREATED");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentUpdatedEvent(CommentUpdatedEvent event) {
        checkAndBlindComment(event.getCommentId(), event.getContent(), "UPDATED");
    }

    private void checkAndBlindComment(Long commentId, String content, String eventType) {
        log.info("Async AI Censorship checking for Comment ID: {} ({})", commentId, eventType);

        boolean isSafe = aiCensorshipService.isContentSafe(content);

        if (!isSafe) {
            log.warn("Comment ID {} detected as UNSAFE. Blinding comment.", commentId);
            commentService.blindComment(commentId);
        } else {
            log.info("Comment ID {} is SAFE.", commentId);
        }
    }
}
