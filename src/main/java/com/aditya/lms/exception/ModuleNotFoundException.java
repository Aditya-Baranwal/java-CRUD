package com.aditya.lms.exception;

import org.springframework.http.HttpStatus;

public class ModuleNotFoundException extends BaseException {

    public ModuleNotFoundException(Long moduleId) {
        super("Module not found for id: " + moduleId, "MODULE_004", HttpStatus.NOT_FOUND);
    }
}
