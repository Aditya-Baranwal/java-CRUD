package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class EnrollmentForbiddenException extends BaseException {

    public EnrollmentForbiddenException(String message) {
        super(message, "ENROLLMENT_403", HttpStatus.FORBIDDEN);
    }

    public EnrollmentForbiddenException(ErrorMessages.Error error) {
        super(error.message(), error.code(), HttpStatus.FORBIDDEN);
    }
}
