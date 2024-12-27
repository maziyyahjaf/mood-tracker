package com.example.maziyyah.mood_tracker.repository;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.example.maziyyah.mood_tracker.constant.Constant;
import com.example.maziyyah.mood_tracker.util.Utils;

@Repository
public class EncouragementMessageRepo {

    @Qualifier(Utils.template01)
    private final RedisTemplate<String, Object> template;
    
    @Qualifier(Utils.template02)
    private final RedisTemplate<String, String> template2;

    public EncouragementMessageRepo(@Qualifier(Utils.template01) RedisTemplate<String, Object> template, @Qualifier(Utils.template02) RedisTemplate<String, String> template2) {
        this.template = template;
        this.template2 = template2;
    }
    
    public void saveMessageToRedis(String userId, String message) {
        String redisKey = Constant.SAVED_ENCOURAGEMENT_MESSAGE_KEY + userId;
        template2.opsForValue().set(redisKey, message, Duration.ofHours(24));
    }

    public String getMessageFromRedis(String userId) {
        String redisKey = Constant.SAVED_ENCOURAGEMENT_MESSAGE_KEY + userId;
        return template2.opsForValue().get(redisKey);
    }

    public Object getUserName(String userId) {
        String redisKey = Constant.USER_KEY_PREFIX + userId;
        return template.opsForHash().get(redisKey, "name");
    }
    
    
}
