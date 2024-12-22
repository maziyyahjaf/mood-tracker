package com.example.maziyyah.mood_tracker.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import com.example.maziyyah.mood_tracker.constant.Constant;
import com.example.maziyyah.mood_tracker.model.User;
import com.example.maziyyah.mood_tracker.util.Utils;

@Repository
public class UserRepository {

    @Qualifier(Utils.template01)
    private final RedisTemplate<String, Object> template;
    private final PasswordEncoder passwordEncoder;

    public UserRepository(@Qualifier(Utils.template01) RedisTemplate<String, Object> template,
            PasswordEncoder passwordEncoder) {
        this.template = template;
        this.passwordEncoder = passwordEncoder;
    }

    public Boolean addUser(User user, String linkingCode) {
        String userKey = "user:" + user.getUserId(); // Use userId as the primary key
        String usernameKey = "username:" + user.getUsername(); // Secondary index for username

        if (template.hasKey(usernameKey)) {
            // Username already exists
            return false;
        }

        Map<String, String> userHash = new HashMap<>();
        userHash.put("userId", user.getUserId());
        userHash.put("username", user.getUsername());
        userHash.put("password", passwordEncoder.encode(user.getPassword()));
        userHash.put("name", user.getName());
        userHash.put("alertThreshold", String.valueOf(user.getAlertThreshold()));
        userHash.put("encouragementOptIn", String.valueOf(user.isEncouragementOptIn()));
        userHash.put("timeZone", user.getTimeZone());

        template.opsForHash().putAll(userKey, userHash);
        template.opsForValue().set(usernameKey, user.getUserId()); // Save the mapping of username -> userId
        template.opsForHash().put(Constant.linkingCodes, linkingCode, user.getUserId());
        return true;

    }

    // retrieve user details by username, query Redis using the username key
    public Map<Object, Object> getUserByUsername(String username) {
        String usernameKey = "username:" + username;

        // Retrieve userId from the secondary index
        String userId = (String) template.opsForValue().get(usernameKey);

        if (userId == null) {
            return null; // User not found
        }

        // retrieve user details from the primary user hash
        String userKey = "user:" + userId;
        Map<Object, Object> userHash = template.opsForHash().entries(userKey);
        return userHash;
    }

    public Boolean updateUsername(String oldUsername, String newUsername) {
        String oldUsernameKey = "username:" + oldUsername;
        String newUsernameKey = "username:" + newUsername;


        // Check if new username is already taken
        if (template.hasKey(newUsernameKey)) {
            return false;
        }

        // Retrieve the userId associated with the old username
        String userId = (String) template.opsForValue().get(oldUsernameKey);
        if (userId == null) {
            return false; // Old username doesnt exist
        }

        // Update the username in the user hash
        String userKey = "user:" + userId;
        template.opsForHash().put(userKey, "username", newUsername);

        // Update the secondary index
        template.delete(oldUsernameKey);
        template.opsForValue().set(newUsernameKey, userId);
        
        return true;
    }

    public Boolean updatePassword(String username, String newPassword) {
        // Retrieve userId from the secondary index
        String usernameKey = "username:" + username;
        String userId = (String) template.opsForValue().get(usernameKey);

        if (userId == null) {
            return false;
        }
         
        // Update the password in the user hash
        String userKey = "user:" + userId;
        template.opsForHash().put(userKey, "password", passwordEncoder.encode(newPassword));
        return true;
    }

    public Boolean uniqueUsername(String username) {
        String usernameKey = "username:" + username;

       return !template.hasKey(usernameKey);
    }

  

}
