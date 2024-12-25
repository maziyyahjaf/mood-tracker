package com.example.maziyyah.mood_tracker.model;

import java.time.LocalDate;
import java.util.List;

public class MoodInsightsDTO {
    
    private String userId; // ID of the user insight belongs to
    private long epochDay;
    private LocalDate date; // Date of the insights
    private List<String> mostCommonMoodEmoji;
    private Integer totalEntries; // Total number of mood entries
    
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public long getEpochDay() {
        return epochDay;
    }
    public void setEpochDay(long epochDay) {
        this.epochDay = epochDay;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public List<String> getMostCommonMoodEmoji() {
        return mostCommonMoodEmoji;
    }
    public void setMostCommonMoodEmoji(List<String> mostCommonMoodEmoji) {
        this.mostCommonMoodEmoji = mostCommonMoodEmoji;
    }
    public Integer getTotalEntries() {
        return totalEntries;
    }
    public void setTotalEntries(Integer totalEntries) {
        this.totalEntries = totalEntries;
    }

    
}
