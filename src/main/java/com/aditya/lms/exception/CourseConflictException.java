package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class CourseConflictException extends BaseException {

    public CourseConflictException(String message) {
        super(message, "COURSE_409", HttpStatus.CONFLICT);
    }
}
