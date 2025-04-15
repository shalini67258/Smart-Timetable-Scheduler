package com.drk.timetable.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.drk.timetable.model.AppSetting;

@Repository
public interface AppSettingRepository extends JpaRepository<AppSetting, String> {
}
