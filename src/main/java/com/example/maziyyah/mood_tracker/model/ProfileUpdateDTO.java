package com.example.maziyyah.mood_tracker.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public class ProfileUpdateDTO {
    
    @NotEmpty(message = "Oops! It looks like you forgot to enter a username.")
    @Pattern(regexp = "^[a-zA-Z0-9._-]{5,50}$", message = "Your username can be 5-50 characters long and may include letters, numbers, dots (.), underscores (_), or hyphens (-).")
    private String username;
    
    @Pattern(regexp = "^$|.{8,}", message = "To keep your account secure, passwords should be at least 8 characters long if you're updating it.")
    private String password;

    @NotEmpty(message = "We’d love to know your name! Could you fill this in?")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z .'-]{1,48}[a-zA-Z]$", 
    message = "Your name can be 3-50 characters and may include letters, spaces, dots (.), hyphens (-), or apostrophes ('). Let’s keep it simple—no numbers or other symbols.")
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
