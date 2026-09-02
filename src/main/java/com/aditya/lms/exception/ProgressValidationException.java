package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class ProgressValidationException extends BaseException {

    public ProgressValidationException(String message) {
        super(message, "PROGRESS_001", HttpStatus.BAD_REQUEST);
    }
}
