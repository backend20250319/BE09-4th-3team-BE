package io.fundy.fundyserver.review.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ReviewErrorCode {

    // 사용자 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // 프로젝트 관련 에러
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."),

    // 리뷰 관련 에러
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 프로젝트에 리뷰를 작성했습니다."),
    INVALID_REVIEW_CONTENT(HttpStatus.BAD_REQUEST, "리뷰 내용이 올바르지 않습니다."),

    // 권한 관련 에러
    UNAUTHORIZED_REVIEW_ACCESS(HttpStatus.FORBIDDEN, "리뷰에 대한 권한이 없습니다."),
    NOT_ALLOWED_FOR_ADMIN(HttpStatus.FORBIDDEN, "관리자는 후기를 작성할 수 없습니다."), // ✅ 추가
    USER_NOT_PARTICIPATED(HttpStatus.FORBIDDEN, "해당 프로젝트에 참여한 사용자만 후기를 작성할 수 있습니다."); // ✅ 추가

    private final HttpStatus status;
    private final String message;

    ReviewErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}