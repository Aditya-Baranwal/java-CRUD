package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class ProgressNotFoundException extends BaseException {

    public ProgressNotFoundException(Long progressId) {
        super("Progress not found for id: " + progressId, "PROGRESS_002", HttpStatus.NOT_FOUND);
    }
}
