package com.example.maziyyah.mood_tracker.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.example.maziyyah.mood_tracker.constant.Constant;
import com.example.maziyyah.mood_tracker.util.Utils;

@Repository
public class StreakRepository {

    @Qualifier(Utils.template01)
    private final RedisTemplate<String, Object> template;

    public StreakRepository(@Qualifier(Utils.template01) RedisTemplate<String, Object> template) {
        this.template = template;
    }

    public Object getCurrentStreak(String userId) {
        String redisKey = Constant.USER_KEY_PREFIX + userId;
        return template.opsForHash().get(redisKey, Constant.STREAK_COUNT_FIELD);
    }

    public void updateStreakCount(String userId, int currentStreak, long streakStartDate) {
        String redisKey = Constant.USER_KEY_PREFIX + userId;
        template.opsForHash().put(redisKey, Constant.STREAK_COUNT_FIELD, String.valueOf(currentStreak));

        // only update streak start date if this is a new streak
        if (currentStreak == 1) {
            template.opsForHash().put(redisKey, Constant.STREAK_START_DATE_FIELD, String.valueOf(streakStartDate));
        }
    }

    public Object getUserAlertThreshold(String userId) {
        String redisKey = Constant.USER_KEY_PREFIX + userId;
        String hashKey = Constant.ALERT_FIELD;
        return template.opsForHash().get(redisKey, hashKey);
    }

    // get daily summary
    public Object getDailySummaryLog(String userId, long epochDay) {
        String redisKey = Constant.USER_KEY_PREFIX + userId + ":summary";
        String hashKey = String.valueOf(epochDay);
        Object dailySummary = template.opsForHash().get(redisKey, hashKey);

        return dailySummary;
    }

    public Object encouragementOptIn(String userid) {
        String redisKey = Constant.USER_KEY_PREFIX + userid;
        String hashKey = Constant.ENCOURAGEMENT_OPT_IN_FIELD;
        return template.opsForHash().get(redisKey, hashKey);
    }

}
