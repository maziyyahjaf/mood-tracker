package com.example.maziyyah.mood_tracker.service;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.maziyyah.mood_tracker.model.DailyMoodSummary;
import com.example.maziyyah.mood_tracker.model.MoodColor;
import com.example.maziyyah.mood_tracker.model.MoodEmoji;
import com.example.maziyyah.mood_tracker.model.MoodEntry;
import com.example.maziyyah.mood_tracker.model.MoodEntryView;
import com.example.maziyyah.mood_tracker.repository.MoodTrackerRepository;

import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

@Service
public class MoodTrackerService {

    private final MoodTrackerRepository moodTrackerRepository;
    private final StreakService streakService;

    public MoodTrackerService(MoodTrackerRepository moodTrackerRepository, StreakService streakService) {
        this.moodTrackerRepository = moodTrackerRepository;
        this.streakService = streakService;
    }

    public void addMoodEntry(String userId, long timestampEpochMilli, long epochDay, int moodScore, String note, List<String> tags)
            throws JsonProcessingException {

        // check if it's a new day
        long lastEpochDay = getRecordedLastLogDay(userId);
        if (lastEpochDay != -1 && epochDay > lastEpochDay) {
            streakService.updateStreak(userId, lastEpochDay);
        }

        // create the MoodEntry obj -> add a new detailed mood entry
        String color = getColorForMood(moodScore);
        MoodEntry moodEntry = new MoodEntry(timestampEpochMilli, epochDay, moodScore, color, note, tags);
        String moodEntryId = moodEntry.getMoodEntryId();
        JsonObject moodEntryJson = moodEntryToJson(moodEntry);

        // **Save the new mood entry first!**
        moodTrackerRepository.addMoodEntry(userId, epochDay, moodEntryId, moodEntryJson);
    
        // update the summary log
        // **Now fetch the updated mood entries for the day**
        List<MoodEntry> moodEntries = getMoodEntriesForDay(userId, epochDay);

        // **Recalculate daily summary using the updated entries**
        double averageMoodScore = calculateAverageMoodScore(moodEntries);
        int numOfMoodEntries = moodEntries.size();
        String summaryMoodColor = calculateSummaryColor(averageMoodScore);
        DailyMoodSummary dailyMoodSummary = new DailyMoodSummary(epochDay, averageMoodScore, numOfMoodEntries,
                summaryMoodColor);
        JsonObject moodSummaryJson = dailyMoodSummaryToJson(dailyMoodSummary);

        // **Update the daily summary log**
        moodTrackerRepository.updateDailySummaryLog(userId, epochDay, moodSummaryJson);

        // **Record the last log day for the user**
        moodTrackerRepository.recordLastLogDay(userId, epochDay);
       
    }

    public List<MoodEntryView> getMoodEntryViewsForDay(String userId, long epochDay) {
        List<MoodEntry> moodEntries = getMoodEntriesForDay(userId, epochDay);
        List<MoodEntryView> moodEntryViews = moodEntries.stream()
                .map(this::toMoodEntryView)
                .collect(Collectors.toList());
        return moodEntryViews;

    }

    public List<MoodEntry> getMoodEntriesForDay(String userId, long epochDay) {
        Map<Object, Object> entries = moodTrackerRepository.getMoodEntriesForDay(userId, epochDay);
        List<MoodEntry> moodEntries = new ArrayList<>();

        for (Entry<Object, Object> entry : entries.entrySet()) {
            JsonReader jsonReader = Json.createReader(new StringReader(entry.getValue().toString()));
            JsonObject jsonObject = jsonReader.readObject();
            MoodEntry moodEntry = toMoodEntry(jsonObject);
            moodEntries.add(moodEntry);
        }

        return moodEntries;
    }

    // insights for each day?
    public Map<Integer, Long> groupMoodEntriesForDayByMoodScore(List<MoodEntry> entries) {
        return entries.stream()
                        .collect(Collectors.groupingBy(MoodEntry::getMoodScore, Collectors.counting()));
    }

    // return all moods with the highest count
    public List<Integer> getMostCommonMoodScore(List<MoodEntry> entries) {
        Map<Integer, Long> moodScoreCounts = groupMoodEntriesForDayByMoodScore(entries);
        long maxMoodScoreCount = moodScoreCounts.values().stream().max(Long::compare).orElse(0L);
        return moodScoreCounts.entrySet().stream()
                                .filter(entry -> entry.getValue() == maxMoodScoreCount)
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());
    }

    // for end mood score map to the corresponding emoji?
    public List<String> matchMoodScoreToMoodEmoji(List<Integer> commonMoodScoreList) {
        List<String> commonMoodEmojiList = commonMoodScoreList.stream()
                                                                .map(score -> MoodEmoji.getEmojiFor(score))
                                                                .collect(Collectors.toList());
        return commonMoodEmojiList;
    }


    public List<DailyMoodSummary> getWeeklySummary(String userId) {
        long todayEpochDay = LocalDate.now().toEpochDay();
        List<DailyMoodSummary> weeklySummaries = new ArrayList<>();

        for (long epochDay = todayEpochDay - 6; epochDay <= todayEpochDay; epochDay++) {
            getDailyMoodSummary(userId, epochDay).ifPresent(weeklySummaries::add);
        }
        return weeklySummaries;
    }

    public Optional<DailyMoodSummary> getDailyMoodSummary(String userId, long epochDay) {
        Object dailySummaryObject = moodTrackerRepository.getDailySummaryLog(userId, epochDay);
     
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

    public JsonObject moodEntryToJson(MoodEntry moodEntry) {
        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
        
        for (String tag : moodEntry.getTags()) {
            arrBuilder.add(tag);
        }

        JsonObject jsonObject = Json.createObjectBuilder()
                .add("moodEntryId", moodEntry.getMoodEntryId())
                .add("timestamp", moodEntry.getTimestamp())
                .add("epochDay", moodEntry.getEpochDay())
                .add("moodScore", moodEntry.getMoodScore())
                .add("color", moodEntry.getColor())
                .add("note", moodEntry.getNote())
                .add("tags", arrBuilder.build())
                .build();
        return jsonObject;
    }

    public MoodEntryView toMoodEntryView(MoodEntry moodEntry) {
        String moodEntryId = moodEntry.getMoodEntryId();
        Date timestamp = new Date(moodEntry.getTimestamp());
        Integer moodScore = moodEntry.getMoodScore();
        String color = moodEntry.getColor();
        String note = moodEntry.getNote();
        List<String> tags = moodEntry.getTags();

        MoodEntryView moodEntryView = new MoodEntryView(moodEntryId, timestamp, moodScore, color, note, tags);
        return moodEntryView;
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

    public double calculateAverageMoodScore(List<MoodEntry> entries) {
        if (entries.isEmpty()) {
            return 0.0;
        }
        double sum = entries.stream().mapToInt(MoodEntry::getMoodScore).sum();
        double avgMoodScore = sum / entries.size();
        return avgMoodScore;
    }

    public String calculateSummaryColor(double avgMoodScore) {
        return MoodColor.getColorFor(avgMoodScore);
    }

    public String getMoodEmoji(double avgMoodScore) {
        return MoodEmoji.getEmojiFor(avgMoodScore);
    }

    public long getRecordedLastLogDay(String userId) {
        Object lastLogDay = moodTrackerRepository.getRecordedLastLogDay(userId);
        if (lastLogDay == null) {
            return -1;
        }
        try {
            return Long.parseLong(lastLogDay.toString());
        } catch (NumberFormatException e) {
            // possible parsing issues?
            return -1;
        }
                
    }

    public JsonObject dailyMoodSummaryToJson(DailyMoodSummary dailyMoodSummary) {
        JsonObject jsonObject = Json.createObjectBuilder()
                .add("epochDay", dailyMoodSummary.getEpochDay())
                .add("averageMoodScore", dailyMoodSummary.getAverageMoodScore())
                .add("numOfMoodEntries", dailyMoodSummary.getNumOfMoodEntries())
                .add("summaryColor", dailyMoodSummary.getSummaryColor())
                .build();
        return jsonObject;
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

    private String getColorForMood(int moodScore) {
        switch (moodScore) {
            case 2:
                return "#4CAF50";
            case 1:
                return "#81C784";
            case 0:
                return "#FFCA28";
            case -1:
                return "#F4511E";
            case -2:
                return "#D32F2F";
            default:
                return "#E0E0E0";
        }
    }

}
