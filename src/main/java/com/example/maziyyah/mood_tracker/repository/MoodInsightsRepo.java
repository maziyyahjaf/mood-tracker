package com.example.maziyyah.mood_tracker.repository;

import java.util.Map;

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

    // get all entries for a given day
    public Map<Object, Object> getMoodEntriesForDay(String userId, long epochDay) {
        String redisKey = "mood:" + userId + ":" + epochDay;
        Map<Object, Object> moodEntriesForDay = template.opsForHash().entries(redisKey);
        return moodEntriesForDay;
    }

}
