package com.drk.timetable.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.drk.timetable.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}