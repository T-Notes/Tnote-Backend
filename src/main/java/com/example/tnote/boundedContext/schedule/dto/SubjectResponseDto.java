package com.example.tnote.boundedContext.schedule.dto;

import com.example.tnote.boundedContext.schedule.entity.ClassDay;
import com.example.tnote.boundedContext.schedule.entity.Subjects;
import lombok.*;

import java.util.List;

@Builder
@Getter
public class SubjectResponseDto {

    private Long id;
    private String subjectName;
    private String classTime;
    private ClassDay classDay;
    private String classLocation;
    private String memo;
    private String semesterName;  // 연관관계를 위함.

    public static SubjectResponseDto of(Subjects subject) {

        return SubjectResponseDto.builder()
                .id(subject.getId())
                .subjectName(subject.getSubjectName())
                .classTime(subject.getClassTime())
                .classDay(subject.getClassDay())
                .classLocation(subject.getClassLocation())
                .memo(subject.getMemo())
                .semesterName(subject.getSchedule().getSemesterName())
                .build();
    }

    public static List<SubjectResponseDto> of(List<Subjects> subject) {
        return subject.stream()
                .map(SubjectResponseDto::of)
                .toList();
    }
}
