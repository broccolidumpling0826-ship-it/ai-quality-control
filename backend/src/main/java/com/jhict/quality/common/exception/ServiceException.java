package com.jhict.quality.common.exception;

import com.jhict.quality.common.entity.ApiResult;
import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;

    public ServiceException(String message) {
        super(message);
        this.code = ApiResult.CODE_SERVER_ERROR;
    }

    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.code = ApiResult.CODE_SERVER_ERROR;
    }

    public ServiceException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
