package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class LessonValidationException extends BaseException {

    public LessonValidationException(String message) {
        super(message, "LESSON_002", HttpStatus.BAD_REQUEST);
    }
}
