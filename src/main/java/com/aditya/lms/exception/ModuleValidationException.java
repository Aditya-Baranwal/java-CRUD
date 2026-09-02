package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class ModuleValidationException extends BaseException {

    public ModuleValidationException(String message) {
        super(message, "MODULE_002", HttpStatus.BAD_REQUEST);
    }
}
