package com.example.tnote.base.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SubjectsErrorResult {

    SUBJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "subject is not found"),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "schedule is not found")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
