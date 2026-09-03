package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class LessonNotFoundException extends BaseException {

    public LessonNotFoundException(Long lessonId) {
        this(ErrorMessages.lessonNotFound(lessonId));
    }

    public LessonNotFoundException(ErrorMessages.Error error) {
        super(error.message(), error.code(), HttpStatus.NOT_FOUND);
    }
}
