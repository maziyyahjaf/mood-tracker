package com.example.maziyyah.mood_tracker.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class UserDTO {

    @NotEmpty(message = "Please enter a username to continue.")
    @Size(min = 5, message = "Your username needs at least 5 characters. Let’s make it memorable!")
    private String username;

    @NotEmpty(message = "We’d love to know your name. Could you fill it in?")
    @Size(min = 3, message = "Your name needs at least 3 characters. Something short and sweet works!")
    private String name;

    @NotEmpty(message = "A password is required to keep your account secure.")
    @Size(min = 8, message = "Your password should have at least 8 characters to stay strong.")
    private String password;

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
    public String getTimeZone() {
        return timeZone;
    }
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    

    

    
    
}
