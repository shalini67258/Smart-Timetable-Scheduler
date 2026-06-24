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

    // --- DASHBOARD & UTILITIES ---

    private String populateAdminData(String email, String message, Model model) {
        User adminUser = new User("System Administrator", email != null ? email : "admin@drk.in", "ADMIN", "");
        model.addAttribute("user", adminUser);
        model.addAttribute("courses", courseRepository.findAll());
        model.addAttribute("teachers", teacherRepository.findAll());
        model.addAttribute("subjects", subjectRepository.findAll());
        model.addAttribute("classrooms", classroomRepository.findAll());
        model.addAttribute("masterSchedules", timetableEntryRepository.findAll());
        model.addAttribute("yearMappings", yearSubjectMappingRepository.findAll());
        model.addAttribute("statusMsg", message);
        model.addAttribute("sections", getDynamicSectionsFromSettings());
        return "portal/admin-dashboard";
    }

    private List<String> getDynamicSectionsFromSettings() {
        return appSettingRepository.findById("SECTIONS")
                .map(s -> List.of(s.getConfigValue().split(",")))
                .orElse(List.of("CSE-A", "CSE-B", "MECH-A"));
    }

    // --- DELETE MAPPINGS (FIXED 404s) ---

    @GetMapping("/admin/delete-teacher/{id}")
    public String deleteTeacher(@PathVariable("id") Long id, @RequestParam(value="adminEmail", required=false) String email, Model model) {
        teacherRepository.deleteById(id);
        return populateAdminData(email, "Faculty record deleted.", model);
    }

    @GetMapping("/admin/delete-subject/{id}")
    public String deleteSubject(@PathVariable("id") Long id, @RequestParam(value="adminEmail", required=false) String email, Model model) {
        subjectRepository.deleteById(id);
        return populateAdminData(email, "Subject link removed.", model);
    }

    @GetMapping("/admin/delete-classroom/{id}")
    public String deleteClassroom(@PathVariable("id") Long id, @RequestParam(value="adminEmail", required=false) String email, Model model) {
        classroomRepository.deleteById(id);
        return populateAdminData(email, "Classroom removed.", model);
    }

    @GetMapping("/admin/delete-course/{id}")
    public String deleteCourse(@PathVariable("id") Long id, @RequestParam(value="adminEmail", required=false) String email, Model model) {
        courseRepository.deleteById(id);
        return populateAdminData(email, "Course wiped.", model);
    }

    @GetMapping("/admin/delete-mapping/{id}")
    public String deleteMapping(@PathVariable("id") Long id, @RequestParam(value="adminEmail", required=false) String email, Model model) {
        yearSubjectMappingRepository.deleteById(id);
        return populateAdminData(email, "Mapping dropped.", model);
    }

    // --- OTHER POST HANDLERS (Keep your existing @PostMappings as they were) ---
    // Ensure every method calls populateAdminData at the end to refresh the view.
    
    @PostMapping("/admin/run-compiler")
    public String runAutoCompiler(@RequestParam("adminEmail") String email, Model model) {
        return populateAdminData(email, timetableService.generateAutomaticTimetable(), model);
    }
}
