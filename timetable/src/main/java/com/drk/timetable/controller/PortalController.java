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

    // --- AUTH ROUTES ---
    @GetMapping("/") public String showLandingPage() { return "portal/index"; }
    @GetMapping("/login/{role}") public String showLoginPage(@PathVariable("role") String role, Model model) { model.addAttribute("role", role != null ? role.toUpperCase() : "USER"); return "portal/login"; }
    @GetMapping("/register") public String showRegisterPage(Model model) { model.addAttribute("user", new User()); model.addAttribute("dynamicBranches", courseRepository.findAll()); model.addAttribute("sections", getDynamicSectionsFromSettings()); return "portal/register"; }

    @PostMapping("/register/process")
    public String handleRegistration(@ModelAttribute("user") User user, @RequestParam(value = "branch", required = false) String branch,
                                     @RequestParam(value = "academicYear", required = false) String year, @RequestParam(value = "sectionName", required = false) String sectionName, Model model) {
        if ("FACULTY".equalsIgnoreCase(user.getRole())) {
            Optional<Teacher> whitelistedTeacher = teacherRepository.findAll().stream().filter(t -> t.getEmail() != null && t.getEmail().equalsIgnoreCase(user.getEmail().trim())).findFirst();
            if (whitelistedTeacher.isEmpty()) { model.addAttribute("errorMsg", "Registration Denied! Your email is not whitelisted."); return "portal/register"; }
            user.setFullName(whitelistedTeacher.get().getName());
        } else if ("STUDENT".equalsIgnoreCase(user.getRole())) { user.setBranch(branch); user.setAcademicYear(year); user.setSectionName(sectionName); }
        if (userService.registerUser(user)) { model.addAttribute("successMsg", "Registration successful!"); return "portal/login"; }
        model.addAttribute("errorMsg", "An account with this email already exists!");
        return "portal/register";
    }

    @PostMapping("/login/process")
    public String handleLogin(@RequestParam("email") String email, @RequestParam("password") String password, Model model) {
        Optional<User> loggedInUser = userService.loginUser(email, password);
        if (loggedInUser.isPresent()) {
            User user = loggedInUser.get();
            if ("ADMIN".equalsIgnoreCase(user.getRole())) return populateAdminData(user.getEmail(), null, model);
            model.addAttribute("user", user);
            if ("FACULTY".equalsIgnoreCase(user.getRole())) { model.addAttribute("mySchedule", timetableEntryRepository.findByTeacherName(user.getFullName())); return "portal/faculty-dashboard"; }
            else { model.addAttribute("mySchedule", timetableEntryRepository.findByAcademicYearAndBranchAndSectionName(user.getAcademicYear(), user.getBranch(), user.getSectionName())); return "portal/student-dashboard"; }
        }
        model.addAttribute("errorMsg", "Invalid Credentials!");
        return "portal/login";
    }

    // --- ADMIN ROUTES ---
    @PostMapping("/admin/run-compiler") public String runAutoCompiler(@RequestParam("adminEmail") String adminEmail, Model model) { return populateAdminData(adminEmail, timetableService.generateAutomaticTimetable(), model); }
    @PostMapping("/admin/clear-timetable") public String clearCompleteTimetable(@RequestParam("adminEmail") String adminEmail, Model model) { timetableEntryRepository.deleteAll(); return populateAdminData(adminEmail, "Timetable wiped.", model); }
    @PostMapping("/admin/manual-assign") public String saveManualOverride(@RequestParam String year, @RequestParam String branch, @RequestParam String section, @RequestParam String day, @RequestParam String timeSlot, @RequestParam String teacher, @RequestParam String subject, @RequestParam String room, @RequestParam String adminEmail, Model model) { timetableEntryRepository.save(new TimetableEntry(year, branch, section, day, timeSlot, teacher, subject, room, "MANUAL")); return populateAdminData(adminEmail, "Manual Entry Saved.", model); }
    @PostMapping("/admin/add-teacher") public String addTeacher(@RequestParam String name, @RequestParam String department, @RequestParam String email, @RequestParam String adminEmail, Model model) { teacherRepository.save(new Teacher(name, department, email)); return populateAdminData(adminEmail, "Faculty added.", model); }
    @PostMapping("/admin/add-classroom") public String addClassroom(@RequestParam String roomNumber, @RequestParam int capacity, @RequestParam String adminEmail, Model model) { classroomRepository.save(new Classroom(roomNumber, capacity)); return populateAdminData(adminEmail, "Classroom added.", model); }
    @PostMapping("/admin/add-subject") public String addSubject(@RequestParam String subjectCode, @RequestParam String subjectName, @RequestParam String branch, @RequestParam String academicYear, @RequestParam String assignedTeacher, @RequestParam String adminEmail, Model model) { subjectRepository.save(new Subject(subjectCode, subjectName, branch, academicYear, assignedTeacher)); return populateAdminData(adminEmail, "Subject added.", model); }
    @PostMapping("/admin/save-settings") public String saveSystemSettings(@RequestParam String startTime, @RequestParam String duration, @RequestParam String shortBreak, @RequestParam String lunchBreak, @RequestParam String sections, @RequestParam String days, @RequestParam String adminEmail, Model model) { appSettingRepository.save(new AppSetting("START_TIME", startTime)); appSettingRepository.save(new AppSetting("PERIOD_DURATION", duration)); appSettingRepository.save(new AppSetting("SHORT_BREAK", shortBreak)); appSettingRepository.save(new AppSetting("LUNCH_BREAK", lunchBreak)); appSettingRepository.save(new AppSetting("SECTIONS", sections)); appSettingRepository.save(new AppSetting("WORKING_DAYS", days)); return populateAdminData(adminEmail, "Settings updated.", model); }

    // THIS IS THE MISSING ROUTE FIXING YOUR 404
    @PostMapping("/admin/map-year-subject")
    public String mapYearToSubject(@RequestParam String academicYear, 
                                   @RequestParam String subjectName, 
                                   @RequestParam String subjectCode, 
                                   @RequestParam String teacherName, 
                                   @RequestParam String adminEmail, Model model) {
        yearSubjectMappingRepository.save(new YearSubjectMapping(academicYear, subjectName, subjectCode, teacherName));
        return populateAdminData(adminEmail, "Mapping saved successfully.", model);
    }

    private String populateAdminData(String email, String message, Model model) {
        model.addAttribute("user", new User("Admin", email, "ADMIN", ""));
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
        String raw = appSettingRepository.findById("SECTIONS").map(AppSetting::getConfigValue).orElse("CSE-A,CSE-B");
        List<String> list = new ArrayList<>();
        for (String s : raw.split(",")) if (!s.isBlank()) list.add(s.trim());
        return list;
    }
}
