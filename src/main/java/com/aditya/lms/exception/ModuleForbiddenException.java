package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class ModuleForbiddenException extends BaseException {

    public ModuleForbiddenException(String message) {
        super(message, "MODULE_403", HttpStatus.FORBIDDEN);
    }

    public ModuleForbiddenException(ErrorMessages.Error error) {
        super(error.message(), error.code(), HttpStatus.FORBIDDEN);
    }
}
