package com.example.maziyyah.mood_tracker.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class UserDTO {

    @NotEmpty(message = "Username cannot be empty")
    @Size(min = 5, message = "Username must be at least 5 characters.")
    private String username;

    @NotEmpty(message = "Name cannot be empty")
    @Size(min = 5, message = "Name must be at least 5 characters.")
    private String name;

    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 9, message = "Password must be at least 9 characters.")
    private String password;

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

    

    
    
}
