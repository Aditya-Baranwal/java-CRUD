package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class UserValidationException extends BaseException {

    public UserValidationException(String message) {
        super(message, "USER_400", HttpStatus.BAD_REQUEST);
    }

    public UserValidationException(ErrorMessages.Error error) {
        super(error.message(), error.code(), HttpStatus.BAD_REQUEST);
    }
}
