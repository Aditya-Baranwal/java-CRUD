package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class LessonConflictException extends BaseException {

    public LessonConflictException(String message) {
        super(message, "LESSON_004", HttpStatus.CONFLICT);
    }
}
