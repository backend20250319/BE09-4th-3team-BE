package io.fundy.fundyserver.register.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

     // 사용자 정의 예외 처리
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        ErrorCode code = ex.getErrorCode();
        ErrorResponse body = ErrorResponse.builder()
                .status(code.getStatus())
                .error(code.name())
                .message(code.getMessage())
                .errors(null)
                .build();
        return ResponseEntity.status(code.getStatus()).body(body);
    }


    //  유효성 검사 예외 처리
    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    public ResponseEntity<ErrorResponse> handleValidationException(Exception ex) {
        List<FieldErrorResponse> fieldErrors = null;

        if (ex instanceof MethodArgumentNotValidException manvEx) {
            fieldErrors = manvEx.getBindingResult().getFieldErrors().stream()
                    .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                    .collect(Collectors.toList());
        } else if (ex instanceof BindException bindEx) {
            fieldErrors = bindEx.getBindingResult().getFieldErrors().stream()
                    .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                    .collect(Collectors.toList());
        }

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .error("VALIDATION_ERROR")
                .message("입력값이 유효하지 않습니다.")
                .errors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    // 잘못된 요청 형식 (ex. 타입 불일치)
    @ExceptionHandler({ IllegalArgumentException.class, MethodArgumentTypeMismatchException.class })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .error("BAD_REQUEST")
                .message("요청 파라미터 형식이 잘못되었습니다.")
                .errors(null)
                .build();
        return ResponseEntity.badRequest().body(body);
    }


    // 모든 처리되지 않은 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknownException(Exception ex) {
        ex.printStackTrace(); // 로그 출력
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .error("INTERNAL_ERROR")
                .message("알 수 없는 서버 오류가 발생했습니다.")
                .errors(null)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
