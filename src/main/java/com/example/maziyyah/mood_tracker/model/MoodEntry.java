package com.example.maziyyah.mood_tracker.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class MoodEntry {

    private String moodEntryId; // UUID for the entry
    private Long timestamp; // Exact moment of logging in UTC (milliseconds)
    private Long epochDay; // The "day" the user logged the entry (local time)
    private String dateString; // human readable date
    private Integer moodScore; // Mood score (-2 to 2)
    private String color; // Corresponding mood color for score
    private String note; // User's note or message
    private String emoji;
    private List<String> tags;
    

    public MoodEntry() {
    }

    public MoodEntry(Long timestamp, Long epochDay, Integer moodScore, String color, String note, List<String> tags) {
        this.moodEntryId = UUID.randomUUID().toString();
        this.timestamp = timestamp;
        this.epochDay = epochDay;
        this.dateString = LocalDate.ofEpochDay(epochDay).toString(); // Convert epochDay to ISO date (e.g., "2024-12-19")
        this.moodScore = moodScore;
        this.color = color;
        this.note = note;
        this.tags = tags;
       
    }
    
    public MoodEntry(String moodEntryId, Long timestamp, Long epochDay, Integer moodScore, String color, String note, List<String> tags) {
        this.moodEntryId = moodEntryId;
        this.timestamp = timestamp;
        this.epochDay = epochDay;
        this.dateString = LocalDate.ofEpochDay(epochDay).toString(); // Convert epochDay to ISO date (e.g., "2024-12-19")
        this.moodScore = moodScore;
        this.color = color;
        this.note = note;
        this.tags = tags;
    }

    public Long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    public Integer getMoodScore() {
        return moodScore;
    }
    public void setMoodScore(Integer moodScore) {
        this.moodScore = moodScore;
    }
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }

    public String getMoodEntryId() {
        return moodEntryId;
    }

    public void setMoodEntryId(String moodEntryId) {
        this.moodEntryId = moodEntryId;
    }

    public Long getEpochDay() {
        return epochDay;
    }

    public void setEpochDay(Long epochDay) {
        this.epochDay = epochDay;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    
}
