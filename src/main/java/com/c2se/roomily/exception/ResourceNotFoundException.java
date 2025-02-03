package com.c2se.roomily.exception;

import com.c2se.roomily.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResourceNotFoundException extends BusinessException{
    public ResourceNotFoundException(String resourceName, String fieldName, String fieldValue){
        super(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, resourceName, fieldName, fieldValue);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
