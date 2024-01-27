package com.example.tnote.base.exception.schedule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ScheduleErrorResult {
    SUBJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "subject is not found"),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "schedule is not found")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}