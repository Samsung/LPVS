package com.lpvs.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class PageControllerAdvice {

    private ErrorResponse errorResponse;

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<ErrorResponse> loginFailedHandle(LoginFailedException e) {
        log.info("loginFailed");
        errorResponse = new ErrorResponse(e.getMessage(), HttpStatus.UNAUTHORIZED.name(), HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

}
