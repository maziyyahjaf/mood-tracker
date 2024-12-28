package com.example.maziyyah.mood_tracker.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public class ProfileUpdateDTO {
    
    @NotEmpty(message = "Oops! It looks like you forgot to enter a username.")
    @Pattern(regexp = "^|[a-zA-Z0-9._-]{5,50}$", message = "Your username should be 5-50 characters and can include letters, numbers, dots, underscores, or hyphens.")
    private String username;
    
    @Pattern(regexp = "^$|.{8,}", message = "To keep your account secure, passwords should be at least 8 characters long if you're updating it.")
    private String password;

    @NotEmpty(message = "We’d love to know your name! Could you fill this in?")
    @Pattern(regexp = "^|[a-zA-Z .'-]{3,50}$", message = "Your name should be 3-50 characters and can include letters, spaces, dots, hyphens, or apostrophes.")
    private String name;

    @Min(1)
    @Max(5)
    private Integer alertThreshold;

    private boolean encouragementOptIn;

    @NotEmpty(message = "Please select a timezone to make sure everything runs smoothly.")
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
