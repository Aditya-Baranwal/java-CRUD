package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class LessonNotFoundException extends BaseException {

    public LessonNotFoundException(Long lessonId) {
        super("Lesson not found for id: " + lessonId, "LESSON_005", HttpStatus.NOT_FOUND);
    }
}
