package com.example.highfive.global.error.dto;

import com.example.highfive.global.error.common.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final int status;
    private String errorMessage;

    private ErrorResponse(final ErrorCode errorCode){
        this.status = errorCode.getStatus();
        this.errorMessage = errorCode.getMessage();
    }

    public static ErrorResponse create(final ErrorCode errorCode){
        return new ErrorResponse(errorCode);
    }
}
