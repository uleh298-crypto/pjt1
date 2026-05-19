package com.ssafy.ssabre.comment.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CommentUpdatedEvent {
    private final Long commentId;
    private final String content;
}
