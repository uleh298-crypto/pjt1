package com.ssafy.ssabre.post.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PostCreatedEvent {
    private final Long postId;
    private final String title;
    private final String content;
}
