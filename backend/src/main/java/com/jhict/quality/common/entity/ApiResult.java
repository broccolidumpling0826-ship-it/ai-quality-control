package com.jhict.quality.common.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ApiResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int CODE_SUCCESS = 2000;
    public static final int CODE_SERVER_ERROR = 5000;
    public static final int CODE_UNAUTHORIZED = 4010;
    public static final int CODE_FORBIDDEN = 4030;
    public static final int CODE_NOT_FOUND = 4040;
    public static final int CODE_BAD_REQUEST = 4000;

    @ApiModelProperty(value = "响应码")
    private int code;

    @ApiModelProperty(value = "响应消息")
    private String message;

    @ApiModelProperty(value = "响应数据")
    private T data;

    @ApiModelProperty(value = "是否成功")
    private boolean success;

    private ApiResult() {
    }

    private ApiResult(int code, String message, T data, boolean success) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = success;
    }

    public static <T> ApiResult<T> success() {
        return new ApiResult<>(CODE_SUCCESS, "操作成功", null, true);
    }

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(CODE_SUCCESS, "操作成功", data, true);
    }

    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(CODE_SUCCESS, message, data, true);
    }

    public static <T> ApiResult<T> failure(String message) {
        return new ApiResult<>(CODE_SERVER_ERROR, message, null, false);
    }

    public static <T> ApiResult<T> failure(int code, String message) {
        return new ApiResult<>(code, message, null, false);
    }

    public static <T> ApiResult<T> failure(int code, String message, T data) {
        return new ApiResult<>(code, message, data, false);
    }
}
