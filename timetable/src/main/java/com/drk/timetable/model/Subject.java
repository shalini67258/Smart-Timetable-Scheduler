package com.drk.timetable.model;

import jakarta.persistence.*;

@Entity
@Table(name = "subjects")
public class Subject {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String subjectCode, subjectName, branch, assignedTeacher, academicYear;

    public Subject() {} // REQUIRED

    public Subject(String subjectCode, String subjectName, String branch, String academicYear, String assignedTeacher) {
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.branch = branch;
        this.academicYear = academicYear;
        this.assignedTeacher = assignedTeacher;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getAssignedTeacher() { return assignedTeacher; }
    public void setAssignedTeacher(String assignedTeacher) { this.assignedTeacher = assignedTeacher; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
}
