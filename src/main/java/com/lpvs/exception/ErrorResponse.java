package com.lpvs.exception;

import java.time.LocalDateTime;

public class ErrorResponse {

    private LocalDateTime timestamp = LocalDateTime.now();
    private String message;
    private String code;
    private int status;


    public ErrorResponse(String message, String code, int status) {
        this.message = message;
        this.code = code;
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }
}
