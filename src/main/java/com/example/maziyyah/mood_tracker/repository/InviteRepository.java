package com.example.maziyyah.mood_tracker.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.example.maziyyah.mood_tracker.util.Utils;

@Repository
public class InviteRepository {

    @Qualifier(Utils.template01)
    private final RedisTemplate<String, Object> template;

    public InviteRepository(@Qualifier(Utils.template01) RedisTemplate<String, Object> template) {
        this.template = template;
    }

    public String getInviterUserDetails(String inviteToken) {
        // redis key for invite invite:{inviteToken}
        String inviteKey = "invite:" + inviteToken;
        String userId = (String) template.opsForHash().get(inviteKey, "userId");
        String name = getUserIdDetails(userId);
        return name;
    }

    public String getUserIdDetails(String userId) {
        String userKey = "user:" + userId;
        String name = (String) template.opsForHash().get(userKey, "name");

        return name;
        
    }



    
    
}
