package com.drk.timetable.model;

import jakarta.persistence.*;

@Entity
@Table(name = "year_subject_mappings")
public class YearSubjectMapping {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String academicYear;
    private String subjectName;
    private String subjectCode;
    private String teacherName;

    public YearSubjectMapping() {}

    public YearSubjectMapping(String academicYear, String subjectName, String subjectCode, String teacherName) {
        this.academicYear = academicYear;
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.teacherName = teacherName;
    }

    // Getters and Setters
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getAcademicYear() { return academicYear; } public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public String getSubjectName() { return subjectName; } public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getSubjectCode() { return subjectCode; } public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getTeacherName() { return teacherName; } public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
}
