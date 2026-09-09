package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class ModuleNotFoundException extends BaseException {

    public ModuleNotFoundException(Long moduleId) {
        this(ErrorMessages.moduleNotFound(moduleId));
    }

    public ModuleNotFoundException(ErrorMessages.Error error) {
        super(error.message(), error.code(), HttpStatus.NOT_FOUND);
    }
}
