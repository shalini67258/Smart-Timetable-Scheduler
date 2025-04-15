package com.drk.timetable.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_settings")
public class AppSetting {

    @Id
    @Column(name = "config_key")
    private String id; // Uses 'id' internally to perfectly map to standard Spring Data JpaRepository methods like findById()

    @Column(nullable = false, length = 1000, name = "config_value")
    private String configValue; // Stores configurations like "09:20", "50", or a comma-separated list of sections

    // Standard no-arg constructor required by JPA Hibernate
    public AppSetting() {}

    // Overloaded constructor used explicitly by your PortalController when updating global configurations
    public AppSetting(String id, String configValue) {
        this.id = id;
        this.configValue = configValue;
    }

    // Standard Getters and Setters for Spring Data compatibility
    public String getId() { 
        return id; 
    }
    
    public void setId(String id) { 
        this.id = id; 
    }

    public String getConfigValue() { 
        return configValue; 
    }
    
    public void setConfigValue(String configValue) { 
        this.configValue = configValue; 
    }
    
    /**
     * ALIAS METHOD: getConfigKey()
     * This provides backwards-compatibility so that any other service or controller 
     * calling .getConfigKey() continues to function perfectly without code modification.
     */
    public String getConfigKey() { 
        return this.id; 
    }
    
    /**
     * ALIAS METHOD: setConfigKey()
     * Updates the underlying primary key field while matching your existing naming structure.
     */
    public void setConfigKey(String configKey) { 
        this.id = configKey; 
    }
}