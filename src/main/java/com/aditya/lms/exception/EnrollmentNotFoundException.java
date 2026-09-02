package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class EnrollmentNotFoundException extends BaseException {

    public EnrollmentNotFoundException(Long enrollmentId) {
        super("Enrollment not found for id: " + enrollmentId, "ENROLLMENT_004", HttpStatus.NOT_FOUND);
    }
}
