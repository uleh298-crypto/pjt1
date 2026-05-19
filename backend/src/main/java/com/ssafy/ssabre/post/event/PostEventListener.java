package com.ssafy.ssabre.post.event;

import com.ssafy.ssabre.global.service.AiCensorshipService;
import com.ssafy.ssabre.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventListener {

    private final AiCensorshipService aiCensorshipService;
    private final PostService postService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostCreatedEvent(PostCreatedEvent event) {
        log.info("Async AI Censorship checking for Post ID: {}", event.getPostId());

        String combinedContent = "Title: " + event.getTitle() + "\nContent: " + event.getContent();
        boolean isSafe = aiCensorshipService.isContentSafe(combinedContent);

        if (!isSafe) {
            log.warn("Post ID {} detected as UNSAFE. Blinding post.", event.getPostId());
            postService.blindPost(event.getPostId());
        } else {
            log.info("Post ID {} is SAFE.", event.getPostId());
        }
    }
}
