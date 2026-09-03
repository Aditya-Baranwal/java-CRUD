package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class LessonConflictException extends BaseException {

    public LessonConflictException(String message) {
        super(message, "LESSON_409", HttpStatus.CONFLICT);
    }

    public LessonConflictException(ErrorMessages.Error error) {
        super(error.message(), error.code(), HttpStatus.CONFLICT);
    }
}
