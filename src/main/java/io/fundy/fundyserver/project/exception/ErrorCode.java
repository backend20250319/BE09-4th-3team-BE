package io.fundy.fundyserver.project.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 일반 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    // 사용자 관련 오류
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),

    // 프로젝트 관련 오류
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."),
    PROJECT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "해당 프로젝트는 현재 후원이 불가능합니다."),

    // 카테고리 관련 오류
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
    DUPLICATE_CATEGORY(HttpStatus.BAD_REQUEST, "이미 존재하는 카테고리입니다."),

    // 리워드 관련 오류
    REWARD_NOT_FOUND(HttpStatus.NOT_FOUND, "리워드를 찾을 수 없습니다."),
    REWARD_NOT_MATCHED(HttpStatus.BAD_REQUEST, "리워드가 해당 프로젝트에 속하지 않습니다."),
    REWARD_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "해당 리워드는 품절되었습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
