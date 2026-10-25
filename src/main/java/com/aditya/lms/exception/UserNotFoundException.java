package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseException {

    public UserNotFoundException(Long userId) {
        this(ErrorMessages.userNotFound(userId));
    }

    public UserNotFoundException(ErrorMessages.Error error) {
        super(error.message(), error.code(), HttpStatus.NOT_FOUND);
    }
}
