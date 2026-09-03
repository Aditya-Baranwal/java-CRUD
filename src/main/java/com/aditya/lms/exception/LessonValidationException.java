package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class LessonValidationException extends BaseException {

    public LessonValidationException(String message) {
        super(message, "LESSON_400", HttpStatus.BAD_REQUEST);
    }

    public LessonValidationException(ErrorMessages.Error error) {
        super(error.message(), error.code(), HttpStatus.BAD_REQUEST);
    }
}
