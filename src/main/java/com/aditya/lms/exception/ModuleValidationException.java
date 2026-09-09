package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class ModuleValidationException extends BaseException {

    public ModuleValidationException(String message) {
        super(message, "MODULE_400", HttpStatus.BAD_REQUEST);
    }

    public ModuleValidationException(ErrorMessages.Error error) {
        super(error.message(), error.code(), HttpStatus.BAD_REQUEST);
    }
}
