package com.example.maziyyah.mood_tracker.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.maziyyah.mood_tracker.model.Mood;
import com.example.maziyyah.mood_tracker.model.MoodEntry;
import com.example.maziyyah.mood_tracker.model.MoodInsights;
import com.example.maziyyah.mood_tracker.repository.MoodInsightsRepo;

@Service
public class MoodInsightsService {

    private final MoodInsightsRepo moodInsightsRepo;

    public MoodInsightsService(MoodInsightsRepo moodInsightsRepo) {
        this.moodInsightsRepo = moodInsightsRepo;
    }

    public MoodInsights calculateDailyInsights(String userId, long epochDay, List<MoodEntry> entries) {
        MoodInsights insights = new MoodInsights();

        // Convert epochDay to LocalDate
        LocalDate date = LocalDate.ofEpochDay(epochDay);

        insights.setUserId(userId);
        insights.setEpochDay(epochDay);
        insights.setDate(date);

        List<Integer> mostCommonMoodScore = getMostCommonMoodScore(entries);
        insights.setMostCommonMoodScore(mostCommonMoodScore);

        List<Mood> mostCommonMoods = getMoods(mostCommonMoodScore);
        List<String> mostCommonMoodsLabel = getMostCommonMoodsLabel(mostCommonMoods);
        List<String> mostCommonMoodsEmoji = getMostCommonMoodsEmoji(mostCommonMoods);
        List<String> mostCommonMoodsColor = getMostCommonMoodsColor(mostCommonMoods);

        insights.setMostCommonMood(mostCommonMoodsLabel);
        insights.setMostCommonMoodEmoji(mostCommonMoodsEmoji);
        insights.setMostCommonMoodColors(mostCommonMoodsColor);

        Map<Integer, Long> moodScoreDistribution = getMoodScoreDistribution(entries);
        insights.setMoodScoreDistribution(moodScoreDistribution);

        Integer numOfMoodEntriesForTheDay = totalEntries(entries);
        insights.setTotalEntries(numOfMoodEntriesForTheDay);

        Map<String, Integer> moodsByTimeOfDay = getMoodsByTimeOfDay(userId, entries);
        insights.setMoodsByTimeOfDay(moodsByTimeOfDay);

        return insights;

    }

    // Mood score distribution
    public Map<Integer, Long> getMoodScoreDistribution(List<MoodEntry> entries) {
        return entries.stream()
                .collect(Collectors.groupingBy(MoodEntry::getMoodScore, Collectors.counting()));
    }

    // return all moods with the highest count
    public List<Integer> getMostCommonMoodScore(List<MoodEntry> entries) {
        Map<Integer, Long> moodScoreCounts = getMoodScoreDistribution(entries);
        long maxMoodScoreCount = moodScoreCounts.values().stream().max(Long::compare).orElse(0L);
        return moodScoreCounts.entrySet().stream()
                .filter(entry -> entry.getValue() == maxMoodScoreCount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<Mood> getMoods(List<Integer> commonMoodScoreList) {
        List<Mood> commonMoods = commonMoodScoreList.stream()
                .map(score -> Mood.getMoodForScore(score))
                .collect(Collectors.toList());
        return commonMoods;
    }

    public List<String> getMostCommonMoodsLabel(List<Mood> moods) {
        List<String> mostCommonMoodsLabel = moods.stream()
                .map(mood -> mood.getLabel())
                .collect(Collectors.toList());
        return mostCommonMoodsLabel;
    }

    public List<String> getMostCommonMoodsEmoji(List<Mood> moods) {
        List<String> mostCommonMoodsEmoji = moods.stream()
                .map(mood -> mood.getEmoji())
                .collect(Collectors.toList());
        return mostCommonMoodsEmoji;

    }

    public List<String> getMostCommonMoodsColor(List<Mood> moods) {
        List<String> mostCommonMoodsColor = moods.stream()
                .map(mood -> mood.getColor())
                .collect(Collectors.toList());
        return mostCommonMoodsColor;
    }

    // total entries
    public int totalEntries(List<MoodEntry> entries) {
        return entries.size();
    }

    public Map<String, Integer> getMoodsByTimeOfDay(String userId, List<MoodEntry> entries) {
        Map<String, Integer> timeBuckets = new HashMap<>();

        timeBuckets.put("Morning", 0);
        timeBuckets.put("Afternoon", 0);
        timeBuckets.put("Everning", 0);

        String userTimeZone = getUserTimeZone(userId);
        ZoneId userZone = ZoneId.of(userTimeZone);

        for (MoodEntry entry : entries) {
            ZonedDateTime zonedDateTime = Instant.ofEpochMilli(entry.getTimestamp())
                    .atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(userZone);
            LocalTime time = zonedDateTime.toLocalTime();
            if (time.isBefore(LocalTime.NOON)) {
                timeBuckets.put("Morning", timeBuckets.get("Morning") + 1);
            } else if (time.isBefore(LocalTime.of(18, 0))) {
                timeBuckets.put("Afternoon", timeBuckets.get("Afternoon") + 1);
            } else if (time.isBefore(LocalTime.MIDNIGHT)) {
                timeBuckets.put("Evening", timeBuckets.get("Evening") + 1);
            } else {
                timeBuckets.put("Night", timeBuckets.get("Night") + 1);
            }
        }

        return timeBuckets;

    }

    public String getUserTimeZone(String userId) {
        Object timeZoneObject = moodInsightsRepo.getUserTimeZone(userId);
        if (timeZoneObject == null) {
            return "UTC";
        }
        String timeZone = timeZoneObject.toString();
        return timeZone;
    }

}
