package io.fundy.fundyserver.register.exception;

public class BusinessLogicException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessLogicException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
