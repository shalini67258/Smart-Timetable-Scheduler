package com.drk.timetable.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.drk.timetable.model.*;
import com.drk.timetable.repository.*;
import com.drk.timetable.service.*;

@Controller
public class PortalController {

    private final UserService userService;
    private final TimetableService timetableService;
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final AppSettingRepository appSettingRepository;
    private final YearSubjectMappingRepository yearSubjectMappingRepository;

    public PortalController(UserService userService, TimetableService timetableService,
                            TeacherRepository teacherRepository, ClassroomRepository classroomRepository, 
                            SubjectRepository subjectRepository, CourseRepository courseRepository,
                            TimetableEntryRepository timetableEntryRepository, AppSettingRepository appSettingRepository,
                            YearSubjectMappingRepository yearSubjectMappingRepository) {
        this.userService = userService;
        this.timetableService = timetableService;
        this.teacherRepository = teacherRepository;
        this.classroomRepository = classroomRepository;
        this.subjectRepository = subjectRepository;
        this.courseRepository = courseRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.appSettingRepository = appSettingRepository;
        this.yearSubjectMappingRepository = yearSubjectMappingRepository;
    }

    // --- CRITICAL FIXES FOR 404 ---
    @GetMapping("/")
    public String showLandingPage() { 
        return "index"; // Ensure index.html is in src/main/resources/templates/
    }

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model) {
        return populateAdminData("admin@drk.in", null, model);
    }

    @GetMapping("/login/{role}")
    public String showLoginPage(@PathVariable("role") String role, Model model) {
        model.addAttribute("role", role != null ? role.toUpperCase() : "USER");
        return "portal/login";
    }

    // --- ALL YOUR EXISTING FUNCTIONALITY ---
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("dynamicBranches", courseRepository.findAll()); 
        model.addAttribute("sections", getDynamicSectionsFromSettings());
        return "portal/register";
    }

    @PostMapping("/register/process")
    public String handleRegistration(@ModelAttribute("user") User user, @RequestParam(required = false) String branch,
                                     @RequestParam(required = false) String academicYear, @RequestParam(required = false) String sectionName, Model model) {
        // ... (Keep your original registration logic here) ...
        boolean success = userService.registerUser(user);
        if (success) return "portal/login";
        model.addAttribute("errorMsg", "Registration Failed");
        return "portal/register";
    }

    @PostMapping("/login/process")
    public String handleLogin(@RequestParam("email") String email, @RequestParam("password") String password, Model model) {
        Optional<User> loggedInUser = userService.loginUser(email, password);
        if (loggedInUser.isPresent()) {
            User user = loggedInUser.get();
            if ("ADMIN".equalsIgnoreCase(user.getRole())) return populateAdminData(user.getEmail(), null, model);
            // ... (Keep your faculty/student dashboard logic here) ...
        }
        return "portal/login";
    }

    // ... (Keep all your existing @PostMapping and @GetMapping methods for subjects, teachers, etc.) ...

    // IMPORTANT: Keep your helper methods at the bottom
    private String populateAdminData(String email, String message, Model model) {
        model.addAttribute("user", new User("System Administrator", email, "ADMIN", ""));
        model.addAttribute("courses", courseRepository.findAll());      
        model.addAttribute("teachers", teacherRepository.findAll());    
        model.addAttribute("subjects", subjectRepository.findAll());    
        model.addAttribute("classrooms", classroomRepository.findAll());
        model.addAttribute("masterSchedules", timetableEntryRepository.findAll());
        model.addAttribute("yearMappings", yearSubjectMappingRepository.findAll());
        model.addAttribute("statusMsg", message);
        model.addAttribute("sections", getDynamicSectionsFromSettings());
        return "portal/admin-dashboard"; // Ensure this file is at templates/portal/admin-dashboard.html
    }

    private List<String> getDynamicSectionsFromSettings() {
        return appSettingRepository.findById("SECTIONS")
                .map(s -> List.of(s.getConfigValue().split(",")))
                .orElse(List.of("CSE-A", "CSE-B", "CSE-C", "MECH-A", "MECH-B"));
    }
}
