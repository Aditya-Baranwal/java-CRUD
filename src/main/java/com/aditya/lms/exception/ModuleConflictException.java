package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class ModuleConflictException extends BaseException {

    public ModuleConflictException(String message) {
        super(message, "MODULE_409", HttpStatus.CONFLICT);
    }

    public ModuleConflictException(ErrorMessages.Error error) {
        super(error.message(), error.code(), HttpStatus.CONFLICT);
    }
}
