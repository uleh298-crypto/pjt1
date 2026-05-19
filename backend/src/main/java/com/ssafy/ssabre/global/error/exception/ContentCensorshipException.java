package com.ssafy.ssabre.global.error.exception;

import com.ssafy.ssabre.global.error.ErrorCode;

public class ContentCensorshipException extends RuntimeException {
    
    private final ErrorCode errorCode;

    public ContentCensorshipException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
