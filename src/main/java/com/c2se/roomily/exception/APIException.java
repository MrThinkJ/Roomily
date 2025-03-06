package com.c2se.roomily.exception;

import com.c2se.roomily.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class APIException extends BusinessException {
    public APIException(HttpStatus httpStatus, ErrorCode errorCode, Object... args) {
        super(httpStatus, errorCode, args);
    }

    public APIException(HttpStatus httpStatus, ErrorCode errorCode) {
        super(httpStatus, errorCode);
    }
}
