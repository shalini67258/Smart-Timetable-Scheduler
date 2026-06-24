package com.drk.timetable.model;

import jakarta.persistence.*;

@Entity
@Table(name = "teachers")
public class Teacher {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String name;
    @Column(nullable = false, unique = true) private String email;
    @Column(nullable = false) private String department;

    public Teacher() {} // REQUIRED

    public Teacher(String name, String department, String email) {
        this.name = name;
        this.department = department;
        this.email = email;
    }
    // Getters and Setters
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; } public void setDepartment(String department) { this.department = department; }
}
