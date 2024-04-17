package com.example.tnote.boundedContext.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    private String schoolName;
    private String subject;
    private int career;
    private boolean alarm;

    public boolean hasSchoolName() {
        return schoolName != null;
    }

    public boolean hasSubject() {
        return subject != null;
    }

    public boolean hasCareer() {
        return career != 0;
    }

    public boolean hasAlarm() {
        return alarm != false;
    }
}
