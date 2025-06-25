package io.fundy.fundyserver.register.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;

    private final int status;            // HTTP 상태 코드 (예: 404, 500)
    private final String error;          // 에러 코드명 (예: USER_NOT_FOUND)
    private final String message;        // 상세 메시지
    private final List<FieldErrorResponse> errors; // 필드 유효성 오류 목록 (선택사항)

    @Builder
    public ErrorResponse(HttpStatus status, String error, String message, List<FieldErrorResponse> errors) {
        this.timestamp = LocalDateTime.now();
        this.status = status.value();  // HttpStatus → int 값 추출
        this.error = error;
        this.message = message;
        this.errors = errors;
    }

    // 필드 에러가 없는 경우 사용할 생성자 오버로딩
    public static ErrorResponse of(HttpStatus status, String error, String message) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .errors(null)
                .build();
    }
}
