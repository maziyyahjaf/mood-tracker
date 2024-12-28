package com.example.maziyyah.mood_tracker.model;

import java.time.LocalTime;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class User {


    private String userId; // Autogenerated userID

    private String username;
    private String name;
    private String password;   
    private Integer alertThreshold; // number of bad days before loved ones are notified (optional)
    private boolean encouragementOptIn; // whether the user opts in for surprise encouragements (optional)
    private String timeZone;
    private LocalTime notificationTime;

    

    public User() {
    }


    public User(String username, String name, String password, String timeZone) {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
        this.name = name;
        this.password = password;
        this.timeZone = timeZone;
        this.alertThreshold = 3; // default threshold (if applicable)
        this.encouragementOptIn = true; // default opt-in for encouragements;
        this.notificationTime = LocalTime.of(8,0); // default to 8:00 am;

    }

    // Convert plaintext password into encodedPassword
    public String rawPasswordToEncodedPassword() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(this.password);

        return encodedPassword;
    }

    
    // this may not be necessary if saving as hash (easier look up instead of saving the entire object as string)
    @Override
    public String toString() {
        return userId + "," + username + "," + rawPasswordToEncodedPassword();
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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


    public LocalTime getNotificationTime() {
        return notificationTime;
    }


    public void setNotificationTime(LocalTime notificationTime) {
        this.notificationTime = notificationTime;
    }

    
    

    

    
    
}
