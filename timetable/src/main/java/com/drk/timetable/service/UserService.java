package com.drk.timetable.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.drk.timetable.model.User;
import com.drk.timetable.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return false;
        }
        userRepository.save(user);
        sendWelcomeEmailConsoleFallback(user);
        return true;
    }

    public Optional<User> loginUser(String email, String password) {
        return userRepository.findByEmail(email).filter(u -> u.getPassword().equals(password));
    }

    private void sendWelcomeEmailConsoleFallback(User user) {
        System.out.println("=================================================");
        System.out.println("DRK REGISTRATION MAIL LOG FOR: " + user.getEmail());
        System.out.println("Status: Success | Account Role: " + user.getRole());
        System.out.println("Academic Mapping: " + user.getBranch() + " (" + user.getAcademicYear() + ")");
        System.out.println("=================================================");
    }
}