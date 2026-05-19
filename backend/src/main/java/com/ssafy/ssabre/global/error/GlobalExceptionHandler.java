package com.ssafy.ssabre.global.error;

import com.ssafy.ssabre.global.error.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리기 (Global Exception Handler)
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * [400] @Valid 유효성 검사 실패
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("handleMethodArgumentNotValidException", e);
        final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.INVALID_INPUT, e.getBindingResult());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * [409] 데이터 무결성 위반 (DB 중복 키 등)
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    protected ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            org.springframework.dao.DataIntegrityViolationException e) {
        log.error("handleDataIntegrityViolationException", e);
        final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.DUPLICATE_RESOURCE, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * [405] 지원하지 않는 HTTP 메서드 호출
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        log.error("handleHttpRequestMethodNotSupportedException", e);
        final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.METHOD_NOT_ALLOWED);
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * [Business] 비즈니스 로직 예외
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(final BusinessException e) {
        final ErrorCode errorCode = e.getErrorCode();
        // 403 권한 오류는 정상적인 비즈니스 로직이므로 warn 레벨로 처리
        if (errorCode.getStatus() == 403) {
            log.warn("handleBusinessException: {}", errorCode.getMessage());
        } else {
            log.error("handleBusinessException", e);
        }
        final ErrorResponse response = ErrorResponse.of(errorCode);
        return new ResponseEntity<>(response, HttpStatus.valueOf(errorCode.getStatus()));
    }

    /**
     * [Censorship] 검열 예외
     */
    @ExceptionHandler(com.ssafy.ssabre.global.error.exception.ContentCensorshipException.class)
    protected ResponseEntity<ErrorResponse> handleContentCensorshipException(
            final com.ssafy.ssabre.global.error.exception.ContentCensorshipException e) {
        log.warn("handleContentCensorshipException", e);
        final ErrorCode errorCode = e.getErrorCode();
        final ErrorResponse response = ErrorResponse.of(errorCode);
        return new ResponseEntity<>(response, HttpStatus.valueOf(errorCode.getStatus()));
    }

    /**
     * [400] 잘못된 인자값 (서비스 예외 단순 처리)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("handleIllegalArgumentException", e);
        // 여기서 e.getMessage()는 "사용자를 찾을 수 없습니다" 같은 구체적인 메시지입니다.
        final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.INVALID_INPUT, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * [404] 사용자를 찾을 수 없음 (로그인 등)
     */
    @ExceptionHandler(org.springframework.security.core.userdetails.UsernameNotFoundException.class)
    protected ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
            org.springframework.security.core.userdetails.UsernameNotFoundException e) {
        log.error("handleUsernameNotFoundException", e);
        final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.ENTITY_NOT_FOUND, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * [401] 비밀번호 불일치 등 인증 실패
     */
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    protected ResponseEntity<ErrorResponse> handleBadCredentialsException(
            org.springframework.security.authentication.BadCredentialsException e) {
        log.error("handleBadCredentialsException", e);
        final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.INVALID_INPUT, "아이디 혹은 비밀번호가 틀립니다."); // 보안상 구체적
                                                                                                              // 이유 숨김
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * [500] 서버 내부 오류
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("handleException", e);
        final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
