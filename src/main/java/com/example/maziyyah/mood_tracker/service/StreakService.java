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

    // StreakService should only focus on tracking, calculating, and maintaining mood streaks.
    public StreakService( StreakRepository streakRepository) {
       
        this.streakRepository = streakRepository;
    }

    public void updateStreak(String userId, long epochDay) {

        // get the DailyMoodSummary for the given day
        DailyMoodSummary dailyMoodSummary = getDailyMoodSummary(userId, epochDay)
                                            .orElseThrow(() -> new IllegalStateException("No daily summary for user " + userId + " on epochDay " + epochDay));


        double averageMoodScore = dailyMoodSummary.getAverageMoodScore();
        Integer currentStreak = getCurrentStreak(userId);

        if (averageMoodScore < 0) {
            // bad day: increment streak
            currentStreak += 1;
            // call the TelegramNotificationService -> encouragement
            System.out.println("Bad Day streak increase to: " + currentStreak);
        } else {
            // good day: reset streak
            currentStreak = 0;
            System.out.println("Streak reset to: " + currentStreak);
        }

        streakRepository.updateStreakCount(userId, currentStreak);

        Integer alertThreshold = getUserAlertThreshold(userId);
        if (currentStreak >= alertThreshold) {
            // call the TelegramNotificationService -> alert lovedones
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
            System.err.println("Error parsing daily summary for user " + userId + " on epochDay " + epochDay +": " + e.getMessage());
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
        Integer alertThreshold = (Integer) streakRepository.getUserAlertThreshold(userId);
        return alertThreshold;
    }

   


}
