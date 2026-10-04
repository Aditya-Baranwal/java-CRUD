package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class EnrollmentValidationException extends BaseException {

    public EnrollmentValidationException(String message) {
        super(message, "ENROLLMENT_400", HttpStatus.BAD_REQUEST);
    }

    public EnrollmentValidationException(ErrorMessages.Error error) {
        super(error.message(), error.code(), HttpStatus.BAD_REQUEST);
    }
}
