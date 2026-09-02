package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class CourseNotFoundException extends BaseException {

    public CourseNotFoundException(Long courseId) {
        this(ErrorMessages.courseNotFound(courseId));
    }

    public CourseNotFoundException(ErrorMessages.Error error) {
        super(error.message(), error.code(), HttpStatus.NOT_FOUND);
    }
}
