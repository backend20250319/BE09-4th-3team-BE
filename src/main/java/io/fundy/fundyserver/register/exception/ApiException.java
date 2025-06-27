package io.fundy.fundyserver.register.exception;


// API 요청 중 발생하는 예외를 처리하기 위한 사용자 정의 예외 클래스

public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // 예외 메시지 설정
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
