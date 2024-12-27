package com.example.maziyyah.mood_tracker.repository;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.example.maziyyah.mood_tracker.constant.Constant;
import com.example.maziyyah.mood_tracker.util.Utils;

@Repository
public class EncouragementMessageRepo {

    @Qualifier(Utils.template02)
    private final RedisTemplate<String, String> template;

    public EncouragementMessageRepo(@Qualifier(Utils.template02) RedisTemplate<String, String> template) {
        this.template = template;
    }

    public void saveMessageToRedis(String userId, String message) {
        String redisKey = Constant.SAVED_ENCOURAGEMENT_MESSAGE_KEY + userId;
        template.opsForValue().set(redisKey, message, Duration.ofHours(24));
    }

    public String getMessageFromRedis(String userId) {
        String redisKey = Constant.SAVED_ENCOURAGEMENT_MESSAGE_KEY + userId;
        return template.opsForValue().get(redisKey);
    }
    
    
}
