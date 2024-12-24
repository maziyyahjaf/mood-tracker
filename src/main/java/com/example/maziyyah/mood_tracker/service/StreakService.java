package com.example.maziyyah.mood_tracker.service;

import java.io.StringReader;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.maziyyah.mood_tracker.model.DailyMoodSummary;
import com.example.maziyyah.mood_tracker.repository.StreakRepository;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@Service
public class StreakService {

    private final StreakRepository streakRepository;
    private final TelegramNotificationService telegramNotificationService;

    // StreakService should only focus on tracking, calculating, and maintaining
    // mood streaks.
    public StreakService(StreakRepository streakRepository, TelegramNotificationService telegramNotificationService) {
        this.streakRepository = streakRepository;
        this.telegramNotificationService = telegramNotificationService;
    }

    public void updateStreak(String userId, long epochDay) {
        // get the DailyMoodSummary for the given day
        DailyMoodSummary dailyMoodSummary = getDailyMoodSummary(userId, epochDay)
                .orElseThrow(() -> new IllegalStateException(
                        "No daily summary for user " + userId + " on epochDay " + epochDay));

        double averageMoodScore = dailyMoodSummary.getAverageMoodScore();
        Integer currentStreak = getCurrentStreak(userId);
        System.out.println("currentStreak :" + currentStreak);

        if (averageMoodScore < 0) {
            // bad day: increment streak
            currentStreak += 1;
            // call the TelegramNotificationService -> encouragement
            // check if encouragement opt in is true
            if (encouragementOptIn(userId)) {
                telegramNotificationService.sendEncouragementMessage(userId, epochDay);
            }
            System.out.println("Bad Day streak increase to: " + currentStreak);
        } else {
            // good day: reset streak
            currentStreak = 0;
            // send "good" encouragement message?
            System.out.println("Streak reset to: " + currentStreak);
        }

        streakRepository.updateStreakCount(userId, currentStreak, epochDay);

        Integer alertThreshold = getUserAlertThreshold(userId);
        if (currentStreak >= alertThreshold) {
            // call the TelegramNotificationService -> alert lovedones
            telegramNotificationService.sendNotificationToLovedOnes(userId, currentStreak);
        }
    }

    public Optional<DailyMoodSummary> getDailyMoodSummary(String userId, long epochDay) {
        Object dailySummaryObject = streakRepository.getDailySummaryLog(userId, epochDay);

        if (dailySummaryObject == null) {
            System.out.println("No daily summary found for user " + userId + " on epochDay " + epochDay);
            return Optional.empty(); // no data, so return an empty Optional
        }

        try (JsonReader jsonReader = Json.createReader(new StringReader(dailySummaryObject.toString()))) {
            JsonObject jsonObject = jsonReader.readObject();
            DailyMoodSummary dailyMoodSummary = toDailyMoodSummary(jsonObject);
            return Optional.of(dailyMoodSummary);
        } catch (Exception e) {
            System.err.println("Error parsing daily summary for user " + userId + " on epochDay " + epochDay + ": "
                    + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty(); // return empty optional if parsing fails

    }

    public DailyMoodSummary toDailyMoodSummary(JsonObject jsonObject) {
        long epochDay = jsonObject.getJsonNumber("epochDay").longValue();
        double averageMoodScore = jsonObject.getJsonNumber("averageMoodScore").doubleValue();
        int numOfMoodEntries = jsonObject.getInt("numOfMoodEntries");
        String summaryColor = jsonObject.getString("summaryColor");

        DailyMoodSummary dailyMoodSummary = new DailyMoodSummary(epochDay, averageMoodScore, numOfMoodEntries,
                summaryColor);
        return dailyMoodSummary;
    }

    public Integer getCurrentStreak(String userId) {
        Object currentStreak = streakRepository.getCurrentStreak(userId);

        if (currentStreak == null) {
            return 0;
        }

        return Integer.parseInt(currentStreak.toString());
    }

    public Integer getUserAlertThreshold(String userId) {
        // Assuming Redis returns a string value
        Object alertThresholdObj = streakRepository.getUserAlertThreshold(userId);
        if (alertThresholdObj instanceof Integer) {
            return (Integer) alertThresholdObj;
        } else if (alertThresholdObj != null) {
            try {
                return Integer.valueOf(alertThresholdObj.toString());
            } catch (NumberFormatException e) {
                System.err.println("Failed to convert alertThreshold to Integer: " + alertThresholdObj);
            }
        }
        return 3; // Or provide a default value like 0
    }

    public Boolean encouragementOptIn(String userId) {
       
        Object encouragementOptInObj = streakRepository.encouragementOptIn(userId);
        // If the value is null, use the default value `true`
        if (encouragementOptInObj == null) {
            return true;
        }
        Boolean encouragementOptIn = Boolean.valueOf(encouragementOptInObj.toString());
        return encouragementOptIn;
    }

}
