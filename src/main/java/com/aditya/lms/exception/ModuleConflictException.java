package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class ModuleConflictException extends BaseException {

    public ModuleConflictException(String message) {
        super(message, "MODULE_003", HttpStatus.CONFLICT);
    }
}
