package com.drk.timetable.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "subjects")
public class Subject { // Kept exactly as Subject!

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subjectCode;
    private String subjectName;
    private String branch;
    private String assignedTeacher; // Tracks who teaches this subject
    private String academicYear;    // This is the missing field causing the service errors

    public Subject() {}

    // Getters and Setters
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