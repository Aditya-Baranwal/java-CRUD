package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class UserConflictException extends BaseException {

    public UserConflictException(String message) {
        super(message, "USER_409", HttpStatus.CONFLICT);
    }

    public UserConflictException(ErrorMessages.Error error) {
        super(error.message(), error.code(), HttpStatus.CONFLICT);
    }
}
