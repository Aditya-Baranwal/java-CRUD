package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class ProgressForbiddenException extends BaseException {

    public ProgressForbiddenException(ErrorMessages.Error error) {
        super(error.message(), error.code(), HttpStatus.FORBIDDEN);
    }
}
