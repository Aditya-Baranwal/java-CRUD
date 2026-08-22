package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class CourseNotFoundException extends BaseException {

    public CourseNotFoundException(Long courseId) {
        super("Course not found for id: " + courseId, "COURSE_404", HttpStatus.NOT_FOUND);
    }
}
