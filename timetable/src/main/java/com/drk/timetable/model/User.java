package com.drk.timetable.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String password;
    private String role;         // ADMIN, FACULTY, STUDENT
    private String branch;       // CSE, ECE, MECH, CIVIL
    private String academicYear; // 1st Year, 2nd Year, 3rd Year, 4th Year

    public User() {}

    public User(String fullName, String email, String role, String password) {
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
private String sectionName; 
public String getSectionName() { return sectionName; }
public void setSectionName(String sectionName) { this.sectionName = sectionName; }
}