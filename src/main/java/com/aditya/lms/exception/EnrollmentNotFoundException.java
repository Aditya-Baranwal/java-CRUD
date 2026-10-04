package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class EnrollmentNotFoundException extends BaseException {

    public EnrollmentNotFoundException(Long enrollmentId) {
        this(ErrorMessages.enrollmentNotFound(enrollmentId));
    }

    public EnrollmentNotFoundException(ErrorMessages.Error error) {
        super(error.message(), error.code(), HttpStatus.NOT_FOUND);
    }
}
