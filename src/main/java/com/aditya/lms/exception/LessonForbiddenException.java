package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class LessonForbiddenException extends BaseException {

    public LessonForbiddenException(String message) {
        super(message, "LESSON_403", HttpStatus.FORBIDDEN);
    }

    public LessonForbiddenException(ErrorMessages.Error error) {
        super(error.message(), error.code(), HttpStatus.FORBIDDEN);
    }
}
