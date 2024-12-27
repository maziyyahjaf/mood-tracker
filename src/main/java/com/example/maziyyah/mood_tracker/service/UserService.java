package com.example.maziyyah.mood_tracker.service;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
        String linkingCode = UUID.randomUUID().toString().substring(0, 6);
        return linkingCode;
    }

    public Optional<User> getUserByUsername(String username) {
        Map<Object, Object> userHash = userRepo.getUserByUsername(username);

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

    public List<User> getUsersWithNotificationsDue() {
        List<String> allUserRedisKeys = getAllUsersRedisKey();
        List<User> allUsers = getAllUsers(allUserRedisKeys);

        if (allUsers == null || allUsers.isEmpty()) {
            logger.warn("No registered users. Returning an empty user list.");
            return Collections.emptyList();
        }

        ZonedDateTime nowUTC = ZonedDateTime.now(ZoneId.of("UTC")); // current time in UTC
        return allUsers.stream()
                        .filter(user -> isNotificationDue(user, nowUTC))
                        .collect(Collectors.toList());
    }

    public boolean isNotificationDue(User user, ZonedDateTime nowUTC) {
        String userTimeZone = user.getTimeZone();
        try {
            ZoneId userZoneId = ZoneId.of(userTimeZone); // user's time zone
            // build the user's notification time in their time zone
            ZonedDateTime userNotificationTime = ZonedDateTime.now(userZoneId)
                    .withHour(user.getNotificationTime().getHour())
                    .withMinute(user.getNotificationTime().getMinute())
                    .withSecond(0)
                    .withNano(0);

            // Debug logs for clarity
            logger.info("Current time (UTC): {}", nowUTC);
            logger.info("User notification time (Local): {}", userNotificationTime);

            return userNotificationTime.toInstant().isBefore(nowUTC.toInstant())
                    && userNotificationTime.plusHours(1).toInstant().isAfter(nowUTC.toInstant());
        } catch (DateTimeException e) {
            // Log error for invalid time zone and skip this user
            LoggerFactory.getLogger(UserService.class).error("Invalid time zone for user {}: {}", user.getUserId(),
                    user.getTimeZone(), e);
            return false;
        }

    }

    public List<String> getAllUsersRedisKey() {
        Set<String> userRedisKeySet = userRepo.getAllUsersRedisKey();
        if (userRedisKeySet == null) {
            return new ArrayList<>();
        }
        List<String> userRedisKeyList = userRedisKeySet.stream()
                .collect(Collectors.toList());
        
        return userRedisKeyList;
    }

    public List<User> getAllUsers(List<String> userKeys) {
        if (userKeys == null || userKeys.isEmpty()) {
            logger.warn("userKeys is null or empty. Returning an empty user list.");
            return Collections.emptyList();
        }

        return userKeys.stream()
                .map(key -> {
                    try {
                        return getUserDataFromUserRedisKey(key);
                    } catch (IllegalArgumentException e) {
                        logger.error("Error retrieving user data for user key {}", key, e);
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
