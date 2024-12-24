package com.example.maziyyah.mood_tracker.service;

import java.io.StringReader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.maziyyah.mood_tracker.model.Mood;
import com.example.maziyyah.mood_tracker.model.MoodEntry;
import com.example.maziyyah.mood_tracker.model.MoodInsights;
import com.example.maziyyah.mood_tracker.repository.MoodInsightsRepo;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

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

        Map<String, Map<Mood, Integer>> moodsByTimeOfDay = getMoodsByTimeOfDay(userId, entries);
        insights.setMoodsByTimeOfDay(moodsByTimeOfDay);

        return insights;

    }

    public MoodInsights calculateDailyInsights(String userId, long epochDay) {
        List<MoodEntry> entries = getMoodEntriesForDay(userId, epochDay);
        return calculateDailyInsights(userId, epochDay, entries);
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

    public Map<String, Map<Mood, Integer>> getMoodsByTimeOfDay(String userId, List<MoodEntry> entries) {
        // Outer map groups by time of day; inner map counts mood types
        Map<String, Map<Mood, Integer>> moodsByTimeOfDay = new HashMap<>();

        // initialize time buckets with empty mood counts
        moodsByTimeOfDay.put("Morning", new HashMap<>());
        moodsByTimeOfDay.put("Afternoon", new HashMap<>());
        moodsByTimeOfDay.put("Evening", new HashMap<>());
        moodsByTimeOfDay.put("Late Night", new HashMap<>());

        String userTimeZone = getUserTimeZone(userId);
        ZoneId userZone = ZoneId.of(userTimeZone);

        for (MoodEntry entry : entries) {
            ZonedDateTime zonedDateTime = Instant.ofEpochMilli(entry.getTimestamp())
                    .atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(userZone);
            LocalTime time = zonedDateTime.toLocalTime();

            String timeBucket;
            if (time.isBefore(LocalTime.NOON)) {
                timeBucket = "Morning";
            } else if (time.isBefore(LocalTime.of(18, 0))) {
                timeBucket = "Afternoon";
            } else if (time.isBefore(LocalTime.MIDNIGHT)) {
                timeBucket = "Evening";
            } else {
                timeBucket = "Late Night";
            }

            // get the mood logged in the current entry
            Mood mood = Mood.getMoodForScore(entry.getMoodScore());

            // Update the inner map for the corresponding time bucket
            moodsByTimeOfDay
                    .computeIfAbsent(timeBucket, k -> new HashMap<>())
                    .merge(mood, 1, Integer::sum);
        }

        return moodsByTimeOfDay;

    }

    public String getUserTimeZone(String userId) {
        Object timeZoneObject = moodInsightsRepo.getUserTimeZone(userId);
        if (timeZoneObject == null) {
            return "UTC";
        }
        String timeZone = timeZoneObject.toString();
        return timeZone;
    }

    public List<MoodEntry> getMoodEntriesForDay(String userId, long epochDay) {
        Map<Object, Object> entries = moodInsightsRepo.getMoodEntriesForDay(userId, epochDay);
        List<MoodEntry> moodEntries = new ArrayList<>();

        for (Entry<Object, Object> entry : entries.entrySet()) {
            JsonReader jsonReader = Json.createReader(new StringReader(entry.getValue().toString()));
            JsonObject jsonObject = jsonReader.readObject();
            MoodEntry moodEntry = toMoodEntry(jsonObject);
            moodEntries.add(moodEntry);
        }

        return moodEntries;
    }

    public List<MoodEntry> getBadMoodEntriesForDay(String userId, long epochDay) {
        List<MoodEntry> entries = getMoodEntriesForDay(userId, epochDay);

        // filter entries with bad mood scores
        List<MoodEntry> badMoodEntries = entries.stream()
                                                .filter(entry -> entry.getMoodScore() <= -1) 
                                                .collect(Collectors.toList());
        return badMoodEntries;
    }

    public List<MoodEntry> getGoodMoodEntriesForDay(String userId, long epochDay) {
        List<MoodEntry> entries = getMoodEntriesForDay(userId, epochDay);

        // filter entries with good mood scores
        List<MoodEntry> goodMoodEntries = entries.stream()
                                                .filter(entry -> entry.getMoodScore() >= 1)
                                                .collect(Collectors.toList());
        return goodMoodEntries;
    }

    

    public MoodEntry toMoodEntry(JsonObject jsonObject) {
        String moodEntryId = jsonObject.getString("moodEntryId");
        Long timestamp = jsonObject.getJsonNumber("timestamp").longValue();
        Long epochDay = jsonObject.getJsonNumber("epochDay").longValue();
        Integer moodScore = jsonObject.getInt("moodScore");
        String color = jsonObject.getString("color");
        String note = jsonObject.getString("note");
        JsonArray tagsJsonArray = jsonObject.getJsonArray("tags");

        List<String> tags = new ArrayList<>();
        for (JsonValue tag : tagsJsonArray) {
            tags.add(tag.toString());
        }
        MoodEntry moodEntry = new MoodEntry(moodEntryId, timestamp, epochDay, moodScore, color, note, tags);

        return moodEntry;
    }

}
