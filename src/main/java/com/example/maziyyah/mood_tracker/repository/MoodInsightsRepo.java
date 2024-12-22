package com.example.maziyyah.mood_tracker.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.example.maziyyah.mood_tracker.constant.Constant;
import com.example.maziyyah.mood_tracker.util.Utils;

@Repository
public class MoodInsightsRepo {

    @Qualifier(Utils.template01)
    private final RedisTemplate<String, Object> template;

    public MoodInsightsRepo(@Qualifier(Utils.template01) RedisTemplate<String, Object> template) {
        this.template = template;
    }

      public Object getUserTimeZone(String userId) {
        String userKey = Constant.USER_KEY_PREFIX + userId; 
        String hashKey = "timeZone";
        return template.opsForHash().get(userKey, hashKey);

    }

    
    
}
