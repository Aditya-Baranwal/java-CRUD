package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class ProgressValidationException extends BaseException {

    public ProgressValidationException(String message) {
        super(message, "PROGRESS_001", HttpStatus.BAD_REQUEST);
    }

    public ProgressValidationException(ErrorMessages.Error error) {
        super(error.message(), error.code(), HttpStatus.BAD_REQUEST);
    }
}
