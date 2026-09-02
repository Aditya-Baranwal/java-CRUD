package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class EnrollmentConflictException extends BaseException {

    public EnrollmentConflictException(String message) {
        super(message, "ENROLLMENT_002", HttpStatus.CONFLICT);
    }
}
