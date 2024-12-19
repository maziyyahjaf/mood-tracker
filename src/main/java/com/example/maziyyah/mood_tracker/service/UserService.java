package com.example.maziyyah.mood_tracker.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// import com.example.maziyyah.mood_tracker.exceptions.UsernameNotFoundException;
import com.example.maziyyah.mood_tracker.model.User;
import com.example.maziyyah.mood_tracker.repository.UserRepository;


@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public Boolean addUser(User user, String linkingCode) { 
        return userRepo.addUser(user, linkingCode);
    }

    public String generateLinkingCode() {
        String linkingCode = UUID.randomUUID().toString().substring(0,6);
        return linkingCode;
    }

    public Optional<User> getUserByUsername(String username) {
        Map<Object,Object> userHash = userRepo.getUserByUsername(username);

        // if null -> handle at controller side to show error message?
        if (userHash == null) {
            return Optional.empty();
            // throw new UsernameNotFoundException();
        } 

        // if not null need to map the data retrieved (hash fields) from Redis
        User user = new User();
        user.setUserId((String) userHash.get("userId"));
        user.setUsername((String) userHash.get("username"));
        user.setPassword((String) userHash.get("password"));

        return Optional.of(user);

    }

    public Boolean login(String username, String rawPassword) {

        return getUserByUsername(username)
                .map(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
                .orElse(false);
        
        // Optional<User> user = getUserByUsername(username);
        // String encodedPassword = user.get().getPassword();

        // // Validate the password
        // if (passwordEncoder.matches(rawPassword, encodedPassword)) {
        //     return true;
        // }

        // // need to handle when it is false -> controller -> custom error handling?
        // return false;
    }

    public Boolean updateUsername(String oldUsername, String newUsername) {
        return userRepo.updateUsername(oldUsername, newUsername);
    }

    public Boolean updatePassword(String username, String newPassword) {
        return userRepo.updatePassword(username, newPassword);
    }
    
   

    
}
