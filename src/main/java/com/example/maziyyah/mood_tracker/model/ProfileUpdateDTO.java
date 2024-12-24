package com.example.maziyyah.mood_tracker.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public class ProfileUpdateDTO {
    
    @NotEmpty(message = "Username cannot be empty")
    @Pattern(regexp = "^|[a-zA-Z0-9._-]{5,50}$", message = "Username must be 5-50 characters and contain only letters, numbers, dots, underscores, or hyphens if provided")
    private String username;
    
    @Pattern(regexp = "^$|.{8,}", message = "Password must be at least 8 characters if provided")
    private String password;

    @NotEmpty(message = "Name cannot be empty")
    @Pattern(regexp = "^|[a-zA-Z .'-]{3,50}$", message = "Name must be 3-50 characters and contain only letters, spaces, dots, hyphens, or apostrophes if provided")
    private String name;

    @Min(1)
    @Max(5)
    private int alertThreshold;

    private boolean encouragementOptIn;

    @NotEmpty(message = "Timezone cannot be empty")
    private String timeZone;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getAlertThreshold() {
        return alertThreshold;
    }
    public void setAlertThreshold(int alertThreshold) {
        this.alertThreshold = alertThreshold;
    }
    public boolean isEncouragementOptIn() {
        return encouragementOptIn;
    }
    public void setEncouragementOptIn(boolean encouragementOptIn) {
        this.encouragementOptIn = encouragementOptIn;
    }
    public String getTimeZone() {
        return timeZone;
    }
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    
}
