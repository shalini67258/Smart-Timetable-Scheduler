package com.drk.timetable.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.drk.timetable.model.*;
import com.drk.timetable.repository.*;
import com.drk.timetable.service.TimetableService;
import com.drk.timetable.service.UserService;

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

    @GetMapping("/") public String showLandingPage() { return "portal/index"; }

    @PostMapping("/admin/add-teacher")
    public String addTeacher(@RequestParam String name, @RequestParam String department, @RequestParam String email, @RequestParam String adminEmail, Model model) {
        teacherRepository.save(new Teacher(name, department, email));
        return populateAdminData(adminEmail, "Teacher added.", model);
    }

    @GetMapping("/admin/delete-teacher/{id}")
    public String delT(@PathVariable Long id, @RequestParam String adminEmail, Model model) {
        teacherRepository.deleteById(id); return populateAdminData(adminEmail, "Deleted.", model);
    }

    @PostMapping("/admin/add-classroom")
    public String addCR(@RequestParam String roomNumber, @RequestParam int capacity, @RequestParam String adminEmail, Model model) {
        classroomRepository.save(new Classroom(roomNumber, capacity));
        return populateAdminData(adminEmail, "Classroom added.", model);
    }

    @GetMapping("/admin/delete-classroom/{id}")
    public String delCR(@PathVariable Long id, @RequestParam String adminEmail, Model model) {
        classroomRepository.deleteById(id); return populateAdminData(adminEmail, "Deleted.", model);
    }

    @PostMapping("/admin/add-subject")
    public String addSub(@RequestParam String subjectCode, @RequestParam String subjectName, @RequestParam String branch, 
                             @RequestParam String academicYear, @RequestParam String assignedTeacher, @RequestParam String adminEmail, Model model) {
        subjectRepository.save(new Subject(subjectCode, subjectName, branch, academicYear, assignedTeacher));
        return populateAdminData(adminEmail, "Subject added.", model);
    }

    @GetMapping("/admin/delete-subject/{id}")
    public String delSub(@PathVariable Long id, @RequestParam String adminEmail, Model model) {
        subjectRepository.deleteById(id); return populateAdminData(adminEmail, "Deleted.", model);
    }

    private String populateAdminData(String email, String msg, Model model) {
        model.addAttribute("user", new User("Admin", email, "ADMIN", ""));
        model.addAttribute("teachers", teacherRepository.findAll());
        model.addAttribute("subjects", subjectRepository.findAll());
        model.addAttribute("classrooms", classroomRepository.findAll());
        model.addAttribute("statusMsg", msg);
        model.addAttribute("sections", getDynamicSectionsFromSettings());
        return "portal/admin-dashboard";
    }

    private List<String> getDynamicSectionsFromSettings() {
        String raw = appSettingRepository.findById("SECTIONS").map(AppSetting::getConfigValue).orElse("CSE-A,CSE-B");
        List<String> list = new ArrayList<>();
        for (String s : raw.split(",")) if (!s.isBlank()) list.add(s.trim());
        return list;
    }
}
