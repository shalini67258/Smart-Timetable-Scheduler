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

    // --- CRITICAL: THE ROOT MAPPING ---
    // If this is missing, the base URL returns a 404.
    @GetMapping("/")
    public String showIndex() {
        return "portal/index"; // Points to src/main/resources/templates/portal/index.html
    }

    // --- DASHBOARD ---
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
        return "portal/admin-dashboard"; // Ensure this file exists at /templates/portal/admin-dashboard.html
    }

    private List<String> getDynamicSectionsFromSettings() {
        return appSettingRepository.findById("SECTIONS")
                .map(s -> List.of(s.getConfigValue().split(",")))
                .orElse(List.of("CSE-A", "CSE-B", "MECH-A"));
    }

    // --- ACTIONS ---
    @PostMapping("/admin/add-subject")
    public String addSubject(@RequestParam("subjectCode") String subjectCode, 
                             @RequestParam("subjectName") String subjectName, 
                             @RequestParam("branch") String branch, 
                             @RequestParam("academicYear") String academicYear, 
                             @RequestParam("assignedTeacher") String assignedTeacher, 
                             @RequestParam("adminEmail") String adminEmail, Model model) {
        Subject s = new Subject(); 
        s.setSubjectCode(subjectCode); 
        s.setSubjectName(subjectName); 
        s.setBranch(branch);
        s.setAcademicYear(academicYear);
        s.setAssignedTeacher(assignedTeacher); 
        subjectRepository.save(s);
        return populateAdminData(adminEmail, "Subject added for " + academicYear, model);
    }

    @GetMapping("/admin/delete-teacher/{id}")
    public String deleteTeacher(@PathVariable("id") Long id, @RequestParam(value="adminEmail", required=false) String email, Model model) {
        teacherRepository.deleteById(id);
        return populateAdminData(email, "Faculty record deleted.", model);
    }
}
