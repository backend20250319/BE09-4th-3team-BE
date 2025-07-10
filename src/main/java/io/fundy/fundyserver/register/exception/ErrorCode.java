package io.fundy.fundyserver.register.exception;

import org.springframework.http.HttpStatus;

// 에러 코드 메시지 출력
public enum ErrorCode {

    // ✅ 사용자 관련 오류
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DUPLICATE_USER_ID(HttpStatus.CONFLICT, "이미 등록된 아이디 입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 등록된 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "이미 등록된 전화번호입니다."),

    // ✅ 인증 관련 오류
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다."),
    BANNED_USER(HttpStatus.FORBIDDEN, "정지된 계정입니다."),

    // ✅ 토큰 관련 오류
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    // ✅ 검증 관련 오류
    VERIFICATION_REQUIRED(HttpStatus.BAD_REQUEST, "전화번호 인증이 완료되지 않았습니다."),
    INVALID_PHONE_FORMAT(HttpStatus.BAD_REQUEST, "전화번호 형식이 잘못되었습니다."),

    // ✅ 권한 관련 오류
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),

    // ✅ 리소스 관련 오류
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),

    // ✅ 배송지 관련 오류
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "배송지를 찾을 수 없습니다."),
    ADDRESS_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 배송지입니다."),
    ADDRESS_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "배송지는 최대 10개까지 등록 가능합니다."),
    ADDRESS_DELETE_FAILED(HttpStatus.BAD_REQUEST, "기본 배송지는 삭제할 수 없습니다."),

    // ✅ 시스템 관련 오류
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "예기치 않은 오류가 발생했습니다."),
    ALREADY_LOGGED_IN(HttpStatus.CONFLICT, "이미 로그인된 상태입니다."),
    PASSWORD_ENCRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "비밀번호 암호화에 실패했습니다."),
    DATABASE_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 저장에 실패했습니다."),

    // ✅ 파일 업로드/저장 관련 오류
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),

    // ✅ 이메일/인증 관련 오류
    NO_SUCH_ALGORITHM(HttpStatus.INTERNAL_SERVER_ERROR, "지원하지 않는 암호화 알고리즘입니다."),
    UNABLE_TO_SEND_EMAIL(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 발송 실패"),
    EMAIL_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다."),
    INVALID_AUTH_CODE(HttpStatus.UNAUTHORIZED, "잘못된 인증코드입니다."),
    AUTH_CODE_EXPIRED(HttpStatus.UNAUTHORIZED, "인증코드가 만료되었습니다."),
    MEMBER_EXISTS(HttpStatus.CONFLICT, "이미 가입된 회원입니다."),
    TOO_FREQUENT_REQUEST(HttpStatus.TOO_MANY_REQUESTS, "인증번호 재발송은 1분에 1회만 가능합니다."),
    DAILY_REQUEST_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "인증번호 요청은 하루 5회로 제한됩니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}