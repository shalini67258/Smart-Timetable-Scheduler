package com.drk.timetable.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.drk.timetable.model.*;
import com.drk.timetable.repository.*;

@Service
public class TimetableService {

    private final ClassroomRepository classroomRepository;
    private final TimetableEntryRepository timetableRepository;
    private final AppSettingRepository appSettingRepository;
    private final YearSubjectMappingRepository yearSubjectMappingRepository;

    public TimetableService(ClassroomRepository classroomRepository, TimetableEntryRepository timetableRepository,
                            AppSettingRepository appSettingRepository, YearSubjectMappingRepository yearSubjectMappingRepository) {
        this.classroomRepository = classroomRepository;
        this.timetableRepository = timetableRepository;
        this.appSettingRepository = appSettingRepository;
        this.yearSubjectMappingRepository = yearSubjectMappingRepository;
    }

    @Transactional
    public String generateAutomaticTimetable() {
        List<Classroom> classrooms = classroomRepository.findAll();
        List<YearSubjectMapping> allMappedSubjects = yearSubjectMappingRepository.findAll();

        // IMPROVED ERROR CHECKING
        if (classrooms.isEmpty()) return "Generation Failed: No Classrooms found in database. Add them in Admin Dashboard.";
        if (allMappedSubjects.isEmpty()) return "Generation Failed: No Year-Subject Mappings found. Please map subjects to years.";

        // --- Logic remains the same, now with cleaner data flow ---
        String startTimeStr = appSettingRepository.findById("START_TIME").map(AppSetting::getConfigValue).orElse("09:20");
        String durationStr = appSettingRepository.findById("PERIOD_DURATION").map(AppSetting::getConfigValue).orElse("50");
        String shortBreakStr = appSettingRepository.findById("SHORT_BREAK").map(AppSetting::getConfigValue).orElse("20");
        String lunchBreakStr = appSettingRepository.findById("LUNCH_BREAK").map(AppSetting::getConfigValue).orElse("40");
        String workingDaysStr = appSettingRepository.findById("WORKING_DAYS").map(AppSetting::getConfigValue).orElse("Monday,Tuesday,Wednesday,Thursday,Friday");
        String configuredSectionsStr = appSettingRepository.findById("SECTIONS").map(AppSetting::getConfigValue).orElse("CSE-A,CSE-B,CSE-C,MECH-A,MECH-B");

        String[] days = workingDaysStr.split(",");
        String[] sections = configuredSectionsStr.split(",");
        String[] years = {"1st Year", "2nd Year", "3rd Year", "4th Year"};

        int duration = Integer.parseInt(durationStr);
        int shortBreak = Integer.parseInt(shortBreakStr);
        int lunchBreak = Integer.parseInt(lunchBreakStr);

        List<TimetableEntry> manualEntries = timetableRepository.findAll().stream()
                .filter(e -> "MANUAL".equalsIgnoreCase(e.getGenerationMode())).toList();
        
        timetableRepository.deleteAll();
        timetableRepository.saveAll(manualEntries);

        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
        List<TimetableEntry> generatedEntriesBuffer = new ArrayList<>();

        for (String day : days) {
            String currentDay = day.trim();
            LocalTime currentTime = LocalTime.parse(startTimeStr, tf);
            
            for (int period = 1; period <= 7; period++) {
                if (period == 3) currentTime = currentTime.plusMinutes(shortBreak);
                if (period == 5) currentTime = currentTime.plusMinutes(lunchBreak);
                LocalTime endTime = currentTime.plusMinutes(duration);
                String slotLabel = currentTime.format(tf) + " - " + endTime.format(tf);
                
                Set<String> occupiedTeachers = new HashSet<>();
                Set<String> occupiedRooms = new HashSet<>();

                // Add manual constraints
                for (TimetableEntry me : manualEntries) {
                    if (me.getDayOfWeek().equalsIgnoreCase(currentDay) && me.getTimeSlot().equalsIgnoreCase(slotLabel)) {
                        if (me.getTeacherName() != null) occupiedTeachers.add(me.getTeacherName().trim().toLowerCase());
                        if (me.getRoomNumber() != null) occupiedRooms.add(me.getRoomNumber().trim().toLowerCase());
                    }
                }

                for (String year : years) {
                    for (String section : sections) {
                        String targetSection = section.trim();
                        String branch = targetSection.contains("-") ? targetSection.split("-")[0] : targetSection;

                        boolean blockOverridden = manualEntries.stream().anyMatch(e -> 
                            e.getDayOfWeek().equalsIgnoreCase(currentDay) && e.getTimeSlot().equalsIgnoreCase(slotLabel)
                            && e.getAcademicYear().equalsIgnoreCase(year) && e.getSectionName().equalsIgnoreCase(targetSection));
                        
                        if (blockOverridden) continue;

                        List<YearSubjectMapping> targetedSubjects = allMappedSubjects.stream()
                                .filter(s -> year.equalsIgnoreCase(s.getAcademicYear()))
                                .collect(Collectors.toList());

                        if (targetedSubjects.isEmpty()) continue;

                        Collections.shuffle(targetedSubjects);
                        for (YearSubjectMapping candidateSub : targetedSubjects) {
                            String tName = (candidateSub.getTeacherName() != null) ? candidateSub.getTeacherName().trim().toLowerCase() : "";
                            if (tName.isEmpty() || !occupiedTeachers.contains(tName)) {
                                Optional<Classroom> freeRoom = classrooms.stream()
                                        .filter(r -> !occupiedRooms.contains(r.getRoomNumber().trim().toLowerCase()))
                                        .findFirst();

                                if (freeRoom.isPresent()) {
                                    occupiedTeachers.add(tName);
                                    occupiedRooms.add(freeRoom.get().getRoomNumber().trim().toLowerCase());
                                    generatedEntriesBuffer.add(new TimetableEntry(year, branch, targetSection, currentDay, slotLabel, 
                                                               candidateSub.getTeacherName(), candidateSub.getSubjectName(), 
                                                               freeRoom.get().getRoomNumber(), "AUTOMATIC"));
                                    break; 
                                }
                            }
                        }
                    }
                }
                currentTime = endTime;
            }
        }
        timetableRepository.saveAll(generatedEntriesBuffer);
        return "SUCCESS: Timetable generated successfully!";
    }
}
