package io.fundy.fundyserver.register.exception;

// API 요청 중 발생하는 예외를 처리하기 위한 사용자 정의 예외 클래스
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    // 기본 생성자: ErrorCode에 정의된 메시지를 사용
    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());  // ErrorCode의 메시지를 부모 클래스 Exception에 전달
        this.errorCode = errorCode;
    }

    // 커스텀 메시지를 처리할 수 있는 생성자
    public ApiException(ErrorCode errorCode, String customMessage) {
        super(customMessage != null ? customMessage : errorCode.getMessage());  // 커스텀 메시지가 있으면 사용하고, 없으면 ErrorCode의 메시지를 사용
        this.errorCode = errorCode;
    }

    // ErrorCode 반환
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    // 예외 발생 시 로그를 기록할 수 있는 유틸리티 메소드 추가 (선택사항)
    public void logError() {
        // 로그 기록 예시
        // 이 메소드를 호출하여 발생한 예외를 로그로 기록할 수 있음
        // Logger를 사용하여 예외에 대한 상세한 로그를 남길 수 있습니다.
        // 예시: LoggerFactory.getLogger(ApiException.class).error("Error occurred: {}", getMessage());
    }
}
