package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class EnrollmentConflictException extends BaseException {

    public EnrollmentConflictException(String message) {
        super(message, "ENROLLMENT_409", HttpStatus.CONFLICT);
    }

    public EnrollmentConflictException(ErrorMessages.Error error) {
        super(error.message(), error.code(), HttpStatus.CONFLICT);
    }
}
