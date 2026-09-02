package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class ProgressConflictException extends BaseException {

    public ProgressConflictException(String message) {
        super(message, "PROGRESS_003", HttpStatus.CONFLICT);
    }
}
