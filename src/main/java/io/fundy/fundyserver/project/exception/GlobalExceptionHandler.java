package io.fundy.fundyserver.project.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@org.springframework.stereotype.Component("projectExceptionHandler")
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", errorCode.getHttpStatus().value());
        body.put("error", errorCode.name());
        body.put("message", errorCode.getMessage());
        body.put("errors", null);
        body.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return new ResponseEntity<>(body, errorCode.getHttpStatus());
    }

}

