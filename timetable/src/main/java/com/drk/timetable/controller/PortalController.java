package com.drk.timetable.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.drk.timetable.model.AppSetting;
import com.drk.timetable.model.Classroom;
import com.drk.timetable.model.Course;
import com.drk.timetable.model.Subject;
import com.drk.timetable.model.Teacher;
import com.drk.timetable.model.TimetableEntry;
import com.drk.timetable.model.User;
import com.drk.timetable.model.YearSubjectMapping;
import com.drk.timetable.repository.AppSettingRepository;
import com.drk.timetable.repository.ClassroomRepository;
import com.drk.timetable.repository.CourseRepository;
import com.drk.timetable.repository.SubjectRepository;
import com.drk.timetable.repository.TeacherRepository;
import com.drk.timetable.repository.TimetableEntryRepository;
import com.drk.timetable.repository.YearSubjectMappingRepository;
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

    @GetMapping("/")
    public String showLandingPage() { 
        return "portal/index"; 
    }

    @GetMapping("/login/{role}")
    public String showLoginPage(@PathVariable("role") String role, Model model) {
        model.addAttribute("role", role != null ? role.toUpperCase() : "USER");
        return "portal/login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("dynamicBranches", courseRepository.findAll()); 
        List<String> sectionChoices = getDynamicSectionsFromSettings();
        model.addAttribute("sections", sectionChoices);
        return "portal/register";
    }

    @PostMapping("/register/process")
    public String handleRegistration(@ModelAttribute("user") User user, 
                                     @RequestParam(value = "branch", required = false) String branch,
                                     @RequestParam(value = "academicYear", required = false) String year,
                                     @RequestParam(value = "sectionName", required = false) String sectionName, Model model) {
        
        if ("FACULTY".equalsIgnoreCase(user.getRole())) {
            Optional<Teacher> whitelistedTeacher = teacherRepository.findAll().stream()
                .filter(t -> t.getEmail() != null && t.getEmail().equalsIgnoreCase(user.getEmail().trim()))
                .findFirst();
                
            if (whitelistedTeacher.isEmpty()) {
                model.addAttribute("dynamicBranches", courseRepository.findAll());
                model.addAttribute("sections", getDynamicSectionsFromSettings());
                model.addAttribute("errorMsg", "Registration Denied! Your email is not whitelisted by Admin under Manage Faculty.");
                return "portal/register";
            }
            user.setFullName(whitelistedTeacher.get().getName());
            user.setBranch(null);
            user.setAcademicYear(null);
            user.setSectionName(null);
        } else if ("STUDENT".equalsIgnoreCase(user.getRole())) {
            user.setBranch(branch);
            user.setAcademicYear(year);
            user.setSectionName(sectionName); 
        } else {
            user.setBranch(null);
            user.setAcademicYear(null);
            user.setSectionName(null);
        }
        
        boolean success = userService.registerUser(user);
        if (success) {
            model.addAttribute("role", user.getRole().toUpperCase());
            model.addAttribute("successMsg", "Registration successful! Proceed to Login.");
            return "portal/login"; 
        } else {
            model.addAttribute("dynamicBranches", courseRepository.findAll());
            model.addAttribute("sections", getDynamicSectionsFromSettings());
            model.addAttribute("errorMsg", "An account with this email address already exists!");
            return "portal/register";
        }
    }

    @PostMapping("/login/process")
    public String handleLogin(@RequestParam("email") String email, @RequestParam("password") String password, Model model) {
        Optional<User> loggedInUser = userService.loginUser(email, password);
        
        if (loggedInUser.isPresent()) {
            User user = loggedInUser.get();
            model.addAttribute("user", user);
            
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                return populateAdminData(user.getEmail(), null, model);
            } 
            else if ("FACULTY".equalsIgnoreCase(user.getRole())) {
                model.addAttribute("teacherName", user.getFullName());
                
                Optional<Teacher> currentTeacher = teacherRepository.findAll().stream()
                    .filter(t -> t.getEmail() != null && t.getEmail().equalsIgnoreCase(user.getEmail().trim()))
                    .findFirst();
                
                List<TimetableEntry> facultySchedule = new ArrayList<>();
                if (currentTeacher.isPresent()) {
                    facultySchedule = timetableEntryRepository.findByTeacherName(currentTeacher.get().getName());
                }
                
                model.addAttribute("mySchedule", facultySchedule);
                return "portal/faculty-dashboard";
            } 
            else if ("STUDENT".equalsIgnoreCase(user.getRole())) {
                model.addAttribute("studentYear", user.getAcademicYear());
                model.addAttribute("studentBranch", user.getBranch());
                model.addAttribute("studentSection", user.getSectionName());
                
                List<TimetableEntry> studentSchedule = timetableEntryRepository.findByAcademicYearAndBranchAndSectionName(
                        user.getAcademicYear(), user.getBranch(), user.getSectionName()
                );
                model.addAttribute("mySchedule", studentSchedule);
                return "portal/student-dashboard";
            }
        }
        model.addAttribute("role", "USER");
        model.addAttribute("errorMsg", "Invalid Credentials! Access Blocked.");
        return "portal/login";
    }

    @PostMapping("/admin/run-compiler")
    public String runAutoCompiler(@RequestParam("adminEmail") String adminEmail, Model model) {
        String msg = timetableService.generateAutomaticTimetable();
        return populateAdminData(adminEmail, msg, model);
    }

    @PostMapping("/admin/clear-timetable")
    public String clearCompleteTimetable(@RequestParam("adminEmail") String adminEmail, Model model) {
        timetableEntryRepository.deleteAll();
        return populateAdminData(adminEmail, "SUCCESS: All compiled and manual timetable entries successfully wiped!", model);
    }

    @PostMapping("/admin/manual-assign")
    public String saveManualOverride(@RequestParam("year") String year, @RequestParam("branch") String branch,
                                     @RequestParam("section") String section, @RequestParam("day") String day,
                                     @RequestParam("timeSlot") String timeSlot, @RequestParam("teacher") String teacher,
                                     @RequestParam("subject") String subject, @RequestParam("room") String room,
                                     @RequestParam("adminEmail") String adminEmail, Model model) {
        TimetableEntry manualEntry = new TimetableEntry(year, branch, section, day, timeSlot, teacher, subject, room, "MANUAL");
        timetableEntryRepository.save(manualEntry);
        return populateAdminData(adminEmail, "SUCCESS: Manual Entry Tracked Successfully!", model);
    }

    @PostMapping("/admin/add-teacher")
    public String addTeacher(@RequestParam("name") String name, @RequestParam("department") String department, 
                             @RequestParam("email") String email, @RequestParam("adminEmail") String adminEmail, Model model) {
        Teacher t = new Teacher(); 
        t.setName(name); 
        t.setDepartment(department); 
        t.setEmail(email != null ? email.trim() : "");
        teacherRepository.save(t);
        return populateAdminData(adminEmail, "Faculty registered successfully.", model);
    }

    @GetMapping("/admin/delete-teacher/{id}")
    public String deleteTeacher(@PathVariable("id") Long id, @RequestParam("adminEmail") String adminEmail, Model model) {
        teacherRepository.deleteById(id);
        return populateAdminData(adminEmail, "Faculty record deleted.", model);
    }

    @PostMapping("/admin/add-classroom")
    public String addClassroom(@RequestParam("roomNumber") String roomNumber, @RequestParam("capacity") int capacity, 
                               @RequestParam("adminEmail") String adminEmail, Model model) {
        Classroom c = new Classroom(); 
        c.setRoomNumber(roomNumber); 
        c.setCapacity(capacity);
        classroomRepository.save(c);
        return populateAdminData(adminEmail, "Classroom registered.", model);
    }

    @GetMapping("/admin/delete-classroom/{id}")
    public String deleteClassroom(@PathVariable("id") Long id, @RequestParam("adminEmail") String adminEmail, Model model) {
        classroomRepository.deleteById(id);
        return populateAdminData(adminEmail, "Classroom removed.", model);
    }

  // ... (code above this line stays the same) ...

    @PostMapping("/admin/add-subject")
    public String addSubject(@RequestParam("subjectCode") String subjectCode, 
                             @RequestParam("subjectName") String subjectName, 
                             @RequestParam("branch") String branch, 
                             @RequestParam(value = "academicYear", defaultValue = "Not Set") String academicYear, 
                             @RequestParam("assignedTeacher") String assignedTeacher, 
                             @RequestParam("adminEmail") String adminEmail, Model model) {
        
        Subject s = new Subject(); 
        s.setSubjectCode(subjectCode); 
        s.setSubjectName(subjectName); 
        s.setBranch(branch);
        s.setAcademicYear(academicYear); 
        s.setAssignedTeacher(assignedTeacher); 
        
        subjectRepository.save(s);
        
        return populateAdminData(adminEmail, "Subject " + subjectName + " saved successfully.", model);
    }

// ... (code below this line stays the same) ...

    @GetMapping("/admin/delete-subject/{id}")
    public String deleteSubject(@PathVariable("id") Long id, @RequestParam("adminEmail") String adminEmail, Model model) {
        subjectRepository.deleteById(id);
        return populateAdminData(adminEmail, "Subject link removed.", model);
    }

    @PostMapping("/admin/add-course")
    public String addCourse(@RequestParam("courseName") String courseName, @RequestParam("durationYears") String durationYears, 
                            @RequestParam("adminEmail") String adminEmail, Model model) {
        Course c = new Course(); 
        c.setCourseName(courseName); 
        c.setDurationYears(durationYears);
        courseRepository.save(c);
        return populateAdminData(adminEmail, "Course tracked.", model);
    }

    @GetMapping("/admin/delete-course/{id}")
    public String deleteCourse(@PathVariable("id") Long id, @RequestParam("adminEmail") String adminEmail, Model model) {
        courseRepository.deleteById(id);
        return populateAdminData(adminEmail, "Course wiped.", model);
    }

    @PostMapping("/admin/map-year-subject")
    public String mapYearSubject(@RequestParam("academicYear") String academicYear,
                                 @RequestParam("subjectId") Long subjectId,
                                 @RequestParam("adminEmail") String adminEmail, Model model) {

        Subject globalSubject = subjectRepository.findById(subjectId).orElse(null);
        String executionMessage;
        
        if (globalSubject != null) {
            YearSubjectMapping newMapping = new YearSubjectMapping(
                academicYear,
                globalSubject.getSubjectName(),
                globalSubject.getSubjectCode(),
                globalSubject.getAssignedTeacher()
            );
            yearSubjectMappingRepository.save(newMapping);
            executionMessage = "Successfully linked " + globalSubject.getSubjectName() + " to " + academicYear + " matrix configurations!";
        } else {
            executionMessage = "Mapping operation aborted: Subject tracking asset reference not resolved.";
        }

        return populateAdminData(adminEmail, executionMessage, model);
    }

    @GetMapping("/admin/delete-mapping/{id}")
    public String deleteMapping(@PathVariable("id") Long id, 
                                @RequestParam("adminEmail") String adminEmail, Model model) {
        yearSubjectMappingRepository.deleteById(id);
        return populateAdminData(adminEmail, "Matrix structural curriculum route dropped successfully.", model);
    }

    @PostMapping("/admin/save-settings")
    public String saveSystemSettings(@RequestParam("startTime") String startTime, @RequestParam("duration") String duration,
                                     @RequestParam("shortBreak") String shortBreak, @RequestParam("lunchBreak") String lunchBreak,
                                     @RequestParam("sections") String sections, @RequestParam("days") String days,
                                     @RequestParam("adminEmail") String adminEmail, Model model) {
        appSettingRepository.save(new AppSetting("START_TIME", startTime));
        appSettingRepository.save(new AppSetting("PERIOD_DURATION", duration));
        appSettingRepository.save(new AppSetting("SHORT_BREAK", shortBreak));
        appSettingRepository.save(new AppSetting("LUNCH_BREAK", lunchBreak));
        appSettingRepository.save(new AppSetting("SECTIONS", sections));
        appSettingRepository.save(new AppSetting("WORKING_DAYS", days));
        return populateAdminData(adminEmail, "Global parameters updated.", model);
    }

    private String populateAdminData(String email, String message, Model model) {
        User adminUser = new User("System Administrator", email, "ADMIN", "");
        
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
        Optional<AppSetting> sectionsSetting = appSettingRepository.findById("SECTIONS");
        String rawSections = sectionsSetting.map(AppSetting::getConfigValue)
                .orElse("CSE-A,CSE-B,CSE-C,MECH-A,MECH-B");
        
        List<String> cleanedSections = new ArrayList<>();
        for (String s : rawSections.split(",")) {
            if (s != null && !s.trim().isEmpty()) {
                cleanedSections.add(s.trim());
            }
        }
        return cleanedSections;
    }
}
