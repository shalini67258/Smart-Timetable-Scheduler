package com.drk.timetable.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.drk.timetable.model.AppSetting;
import com.drk.timetable.model.Classroom;
import com.drk.timetable.model.TimetableEntry;
import com.drk.timetable.model.YearSubjectMapping;
import com.drk.timetable.repository.AppSettingRepository;
import com.drk.timetable.repository.ClassroomRepository;
import com.drk.timetable.repository.TimetableEntryRepository;
import com.drk.timetable.repository.YearSubjectMappingRepository;

@Service
public class TimetableService {

    private final ClassroomRepository classroomRepository;
    private final TimetableEntryRepository timetableRepository;
    private final AppSettingRepository appSettingRepository;
    private final YearSubjectMappingRepository yearSubjectMappingRepository;

    public TimetableService(ClassroomRepository classroomRepository,
                            TimetableEntryRepository timetableRepository,
                            AppSettingRepository appSettingRepository,
                            YearSubjectMappingRepository yearSubjectMappingRepository) {
        this.classroomRepository = classroomRepository;
        this.timetableRepository = timetableRepository;
        this.appSettingRepository = appSettingRepository;
        this.yearSubjectMappingRepository = yearSubjectMappingRepository;
    }

    @Transactional
    public String generateAutomaticTimetable() {
        List<Classroom> classrooms = classroomRepository.findAll();
        // Read directly from your mapped curriculum rules table
        List<YearSubjectMapping> allMappedSubjects = yearSubjectMappingRepository.findAll();

        if (classrooms.isEmpty() || allMappedSubjects.isEmpty()) {
            return "Generation Failed: Ensure Year-Subject Mappings and Classrooms are logged in database pools!";
        }

        // Load configuration strings safely with sensible default fallbacks
        String startTimeStr = appSettingRepository.findById("START_TIME").map(AppSetting::getConfigValue).orElse("09:20");
        String durationStr = appSettingRepository.findById("PERIOD_DURATION").map(AppSetting::getConfigValue).orElse("50");
        String shortBreakStr = appSettingRepository.findById("SHORT_BREAK").map(AppSetting::getConfigValue).orElse("20");
        String lunchBreakStr = appSettingRepository.findById("LUNCH_BREAK").map(AppSetting::getConfigValue).orElse("40");
        String workingDaysStr = appSettingRepository.findById("WORKING_DAYS").map(AppSetting::getConfigValue).orElse("Monday,Tuesday,Wednesday,Thursday,Friday");
        String configuredSectionsStr = appSettingRepository.findById("SECTIONS").map(AppSetting::getConfigValue)
                .orElse("CSE-A,CSE-B,CSE-C,MECH-A,MECH-B,CSD-A,CSC-A,ECE-A,ECE-B,MBA");

        String[] days = workingDaysStr.split(",");
        String[] sections = configuredSectionsStr.split(",");
        String[] years = {"1st Year", "2nd Year", "3rd Year", "4th Year"};

        int duration = Integer.parseInt(durationStr);
        int shortBreak = Integer.parseInt(shortBreakStr);
        int lunchBreak = Integer.parseInt(lunchBreakStr);

        // Retain and isolate existing MANUAL overrides before rewriting auto-slots
        List<TimetableEntry> manualEntries = timetableRepository.findAll().stream()
                .filter(e -> "MANUAL".equalsIgnoreCase(e.getGenerationMode())).toList();
        timetableRepository.deleteAll();
        timetableRepository.saveAll(manualEntries);

        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
        
        Set<String> occupiedTeachers = new HashSet<>();
        Set<String> occupiedRooms = new HashSet<>();
        List<TimetableEntry> generatedEntriesBuffer = new ArrayList<>();

        for (String day : days) {
            day = day.trim();
            final String currentDay = day; 
            
            LocalTime currentTime = LocalTime.parse(startTimeStr, tf);
            
            for (int period = 1; period <= 7; period++) {
                if (period == 3) currentTime = currentTime.plusMinutes(shortBreak); 
                if (period == 5) currentTime = currentTime.plusMinutes(lunchBreak); 

                LocalTime endTime = currentTime.plusMinutes(duration);
                String slotLabel = currentTime.format(tf) + " - " + endTime.format(tf);

                // Reset period-scoped availability tracking blocks
                occupiedTeachers.clear();
                occupiedRooms.clear();

                // Lock out resources claimed via manual overrides for this slot block
                for (TimetableEntry me : manualEntries) {
                    if (me.getDayOfWeek().equalsIgnoreCase(currentDay) && me.getTimeSlot().equalsIgnoreCase(slotLabel)) {
                        if (me.getTeacherName() != null) occupiedTeachers.add(me.getTeacherName().trim().toLowerCase());
                        if (me.getRoomNumber() != null) occupiedRooms.add(me.getRoomNumber().trim().toLowerCase());
                    }
                }

                for (String year : years) {
                    for (String section : sections) {
                        section = section.trim();
                        // Turn "CSE-A" into "CSE" to accurately map generic course assets
                        String branch = section.contains("-") ? section.split("-")[0] : section;

                        final String currentSlot = slotLabel;
                        final String targetSection = section;
                        final String currentYear = year;
                        
                        // Skip if a manual entry exists for this exact coordinates node
                        boolean blockOverridden = manualEntries.stream().anyMatch(e -> 
                            e.getDayOfWeek().equalsIgnoreCase(currentDay) && e.getTimeSlot().equalsIgnoreCase(currentSlot)
                            && e.getAcademicYear().equalsIgnoreCase(currentYear) && e.getSectionName().equalsIgnoreCase(targetSection)
                        );
                        if (blockOverridden) continue;

                        // Filter mapped syllabus records matching the requested academic year group
                        List<YearSubjectMapping> targetedSubjects = allMappedSubjects.stream()
                                .filter(s -> s.getAcademicYear() != null && s.getAcademicYear().equalsIgnoreCase(currentYear))
                                .collect(Collectors.toList());

                        if (targetedSubjects.isEmpty()) continue;

                        Collections.shuffle(targetedSubjects);

                        YearSubjectMapping selectedSubject = null;
                        Classroom selectedRoom = null;

                        for (YearSubjectMapping candidateSub : targetedSubjects) {
                            String teacherName = candidateSub.getTeacherName();
                            String normalizedTeacher = teacherName != null ? teacherName.trim().toLowerCase() : "";
                            
                            // Matrix Resource check: Teacher must be free globally for this current period
                            if (teacherName == null || !occupiedTeachers.contains(normalizedTeacher)) {
                                Optional<Classroom> freeRoom = classrooms.stream()
                                        .filter(r -> !occupiedRooms.contains(r.getRoomNumber().trim().toLowerCase()))
                                        .findFirst();

                                if (freeRoom.isPresent()) {
                                    selectedSubject = candidateSub;
                                    selectedRoom = freeRoom.get();
                                    break;
                                }
                            }
                        }

                        // Generate persistent data model record if assignments align cleanly
                        if (selectedSubject != null && selectedRoom != null) {
                            String assignedTeacherName = selectedSubject.getTeacherName() != null ? selectedSubject.getTeacherName() : "Guest Professor";
                            
                            occupiedTeachers.add(assignedTeacherName.trim().toLowerCase());
                            occupiedRooms.add(selectedRoom.getRoomNumber().trim().toLowerCase());

                            TimetableEntry entry = new TimetableEntry(
                                year, branch, section, currentDay, slotLabel, assignedTeacherName, selectedSubject.getSubjectName(), selectedRoom.getRoomNumber(), "AUTOMATIC"
                            );
                            generatedEntriesBuffer.add(entry);
                        }
                    }
                }
                currentTime = endTime;
            }
        }
        
        // Write generated timetable records downstream in a single batch
        if (!generatedEntriesBuffer.isEmpty()) {
            timetableRepository.saveAll(generatedEntriesBuffer);
        }
        
        return "SUCCESS: Full Matrix Saved to PostgreSQL database across all custom sections!";
    }  
}