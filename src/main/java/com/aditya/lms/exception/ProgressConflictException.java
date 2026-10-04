package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class ProgressConflictException extends BaseException {

    public ProgressConflictException(String message) {
        super(message, "PROGRESS_003", HttpStatus.CONFLICT);
    }

    public ProgressConflictException(ErrorMessages.Error error) {
        super(error.message(), error.code(), HttpStatus.CONFLICT);
    }
}
