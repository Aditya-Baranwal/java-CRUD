package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class CourseValidationException extends BaseException {

    public CourseValidationException(String message) {
        super(message, "COURSE_400", HttpStatus.BAD_REQUEST);
    }
}
