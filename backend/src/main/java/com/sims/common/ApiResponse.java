package com.sims.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    private ApiResponse() {}

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> ok() {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = 200;
        r.message = "success";
        return r;
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = code;
        r.message = message;
        return r;
    }

    public static <T> ApiResponse<T> fail(String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = 500;
        r.message = message;
        return r;
    }
}
