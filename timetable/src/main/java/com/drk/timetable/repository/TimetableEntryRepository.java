package com.drk.timetable.repository;

import com.drk.timetable.model.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {
    List<TimetableEntry> findByTeacherName(String teacherName);
    // Finds personalized timetable for a specific student group
    List<TimetableEntry> findByAcademicYearAndBranchAndSectionName(String year, String branch, String section);
}