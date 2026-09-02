package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class EnrollmentValidationException extends BaseException {

    public EnrollmentValidationException(String message) {
        super(message, "ENROLLMENT_001", HttpStatus.BAD_REQUEST);
    }
}
