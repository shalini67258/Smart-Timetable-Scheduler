package com.drk.timetable.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "timetable_entries")
public class TimetableEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String academicYear; 
    private String branch;       
    private String sectionName; // Will store "CSE-A", "MECH-B", "MBA", etc. 
    private String dayOfWeek;
    private String timeSlot;    // Will dynamically store calculated "09:20 - 10:10"
    private String teacherName;
    private String subjectName;
    private String roomNumber;
    private String generationMode;

    public TimetableEntry() {}

    public TimetableEntry(String academicYear, String branch, String sectionName, String dayOfWeek, 
                          String timeSlot, String teacherName, String subjectName, String roomNumber, String generationMode) {
        this.academicYear = academicYear;
        this.branch = branch;
        this.sectionName = sectionName;
        this.dayOfWeek = dayOfWeek;
        this.timeSlot = timeSlot;
        this.teacherName = teacherName;
        this.subjectName = subjectName;
        this.roomNumber = roomNumber;
        this.generationMode = generationMode;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getGenerationMode() { return generationMode; }
    public void setGenerationMode(String generationMode) { this.generationMode = generationMode; }
}