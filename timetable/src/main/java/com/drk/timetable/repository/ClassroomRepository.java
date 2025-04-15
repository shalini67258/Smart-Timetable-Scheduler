package com.drk.timetable.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.drk.timetable.model.Classroom;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
}