package com.yuhyeon.devwebide.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ProjectAccessDeniedException extends IllegalArgumentException {

    public ProjectAccessDeniedException(String message) {
        super(message);
    }
}
