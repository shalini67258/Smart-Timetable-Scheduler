package com.drk.timetable.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.drk.timetable.model.*;
import com.drk.timetable.repository.*;
import com.drk.timetable.service.*;

@Controller
public class PortalController {

    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;
    private final AppSettingRepository appSettingRepository;
    private final TimetableService timetableService;

    // Constructor Injection
    public PortalController(TeacherRepository teacherRepository, SubjectRepository subjectRepository, 
                            CourseRepository courseRepository, AppSettingRepository appSettingRepository,
                            TimetableService timetableService) {
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.courseRepository = courseRepository;
        this.appSettingRepository = appSettingRepository;
        this.timetableService = timetableService;
    }

    // LOAD LANDING PAGE
    @GetMapping("/")
    public String index() {
        return "index"; 
    }

    // LOAD ADMIN DASHBOARD
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        return populateAdminData("admin@drk.in", null, model);
    }

    private String populateAdminData(String email, String message, Model model) {
        model.addAttribute("user", new User("System Administrator", email, "ADMIN", ""));
        model.addAttribute("courses", courseRepository.findAll());
        model.addAttribute("teachers", teacherRepository.findAll());
        model.addAttribute("subjects", subjectRepository.findAll());
        model.addAttribute("statusMsg", message);
        model.addAttribute("sections", List.of("CSE-A", "CSE-B", "MECH-A"));
        return "portal/admin-dashboard";
    }

    @PostMapping("/admin/add-subject")
    public String addSubject(@RequestParam String subjectCode, @RequestParam String subjectName, 
                             @RequestParam String branch, @RequestParam String academicYear, 
                             @RequestParam String assignedTeacher, @RequestParam String adminEmail, Model model) {
        Subject s = new Subject();
        s.setSubjectCode(subjectCode);
        s.setSubjectName(subjectName);
        s.setBranch(branch);
        s.setAcademicYear(academicYear);
        s.setAssignedTeacher(assignedTeacher);
        subjectRepository.save(s);
        return populateAdminData(adminEmail, "Subject saved!", model);
    }
}
