package com.drk.timetable.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.drk.timetable.model.YearSubjectMapping;

@Repository
public interface YearSubjectMappingRepository extends JpaRepository<YearSubjectMapping, Long> {
    List<YearSubjectMapping> findByAcademicYear(String academicYear);
}