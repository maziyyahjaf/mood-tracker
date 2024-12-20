package com.example.maziyyah.mood_tracker.repository;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.example.maziyyah.mood_tracker.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;


import jakarta.json.JsonObject;


@Repository
public class MoodTrackerRepository {

    @Qualifier(Utils.template01)
    private final RedisTemplate<String, Object> template;
   

    public MoodTrackerRepository(@Qualifier(Utils.template01) RedisTemplate<String, Object> template) {
        this.template = template;
    }

    // redisKey for moodEntries -> mood:{userId}:{epochDay}
    public void addMoodEntry(String userId, long epochDay, String moodEntryId, JsonObject moodEntryJson) throws JsonProcessingException {
        String redisKey = "mood:" + userId + ":" + epochDay;
        String moodData = moodEntryJson.toString();

        template.opsForHash().put(redisKey, moodEntryId, moodData);
    
    }

    // get all entries for a given day
    public Map<Object, Object> getMoodEntriesForDay(String userId, long epochDay) {
        String redisKey = "mood:" + userId + ":" + epochDay;
        Map<Object, Object> moodEntriesForDay = template.opsForHash().entries(redisKey);
        return moodEntriesForDay;
    }

    public void updateDailySummaryLog(String userId, long epochDay, JsonObject dailyMoodSummaryJson) {
        String redisKey = "user:" + userId + ":summary";
        String hashKey = String.valueOf(epochDay);
        String dailySummaryData = dailyMoodSummaryJson.toString();
        template.opsForHash().put(redisKey, hashKey, dailySummaryData);

    }

    // get daily summary
    public Object getDailySummaryLog(String userId, long epochDay) {
        String redisKey = "user:" + userId + ":summary";
        String hashKey = String.valueOf(epochDay);
        Object dailySummary = template.opsForHash().get(redisKey, hashKey);
        
        return dailySummary;
    }

    public void recordLastLogDay(String userId, long epochDay) {
        String redisKey = "user:" + userId + ":last_log_day";
        template.opsForValue().set(redisKey, String.valueOf(epochDay));
    }

    public Object getRecordedLastLogDay(String userId) {
        String redisKey = "user:" + userId + ":last_log_day";
        return template.opsForValue().get(redisKey);
    }



    
    
    
}
