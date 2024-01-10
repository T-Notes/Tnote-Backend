package com.example.tnote.boundedContext.classLog.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassLogImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "classLog_image_id")
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String classLogImageUrl;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "classLog_id")
    private ClassLog classLog;

//    @Builder
//    public ClassLogImage(ClassLog classLog, String classLogImageUrl) {
//        this.classLog = classLog;
//        if (classLog != null) {
//            classLog.getClassLogImage().add(this);
//        }
//        this.classLogImageUrl = classLogImageUrl;
//    }
}
