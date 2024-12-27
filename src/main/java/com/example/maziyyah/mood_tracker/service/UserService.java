package com.example.maziyyah.mood_tracker.service;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// import com.example.maziyyah.mood_tracker.exceptions.UsernameNotFoundException;
import com.example.maziyyah.mood_tracker.model.User;
import com.example.maziyyah.mood_tracker.repository.UserRepository;



@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
        private static final Logger logger = LoggerFactory.getLogger(UserService.class);


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

    public Boolean updatePassword(String userId, String newPassword) {
        return userRepo.updatePassword(userId, newPassword);
    }
    
    public User getUserDetailsByUserId(String userId) {
        Map<Object, Object> userHash = userRepo.getUserDetails(userId);
        
        User user = new User();
        user.setUserId((String) userHash.get("userId"));
        user.setUsername((String) userHash.get("username"));
        user.setPassword((String) userHash.get("password"));
        user.setName((String) userHash.get("name"));
        user.setAlertThreshold(Integer.parseInt((String) userHash.get("alertThreshold")));
        user.setEncouragementOptIn(Boolean.parseBoolean((String) userHash.get("encouragementOptIn")));
        user.setTimeZone((String) userHash.get("timeZone"));
        user.setNotificationTime(LocalTime.parse((String) userHash.get("notificationTime")));

        return user;
    }

    public boolean updateUserDetails(String userId, User user) {
        return userRepo.updateUserDetails(userId, user);
    }

    public Optional<List<String>> getAllUsersRedisKey() {
        Set<Object> userRedisKeyListObj = userRepo.getAllUsersRedisKey();
       
        if (userRedisKeyListObj == null || userRedisKeyListObj.isEmpty()) {
            return Optional.empty();
        }

        List<String> userRedisKeyList = userRedisKeyListObj.stream()
                                                            .map(Object::toString)
                                                            .collect(Collectors.toList());
        return Optional.of(userRedisKeyList);
    }

    public List<User> getAllUsers(Optional<List<String>> userKeys) {
        // If the Optional is empty or contains an empty list, return an empty list
        return userKeys.orElseGet(Collections::emptyList)
                        .stream()
                        .map(userKey -> {
                            try {
                                return getUserDataFromUserRedisKey(userKey);
                            } catch (IllegalArgumentException e) {
                                logger.error("Error retrieving user data for user key {}", userKey, e);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        
    }

    public User getUserDataFromUserRedisKey(String userRedisKey) {
        Map<Object, Object> userHash = userRepo.getUserData(userRedisKey);
        
        User user = new User();
        user.setUserId((String) userHash.get("userId"));
        user.setUsername((String) userHash.get("username"));
        user.setPassword((String) userHash.get("password"));
        user.setName((String) userHash.get("name"));
        user.setAlertThreshold(Integer.parseInt((String) userHash.get("alertThreshold")));
        user.setEncouragementOptIn(Boolean.parseBoolean((String) userHash.get("encouragementOptIn")));
        user.setTimeZone((String) userHash.get("timeZone"));
        user.setNotificationTime(LocalTime.parse((String) userHash.get("notificationTime")));

        return user;
    }
   

    
}
