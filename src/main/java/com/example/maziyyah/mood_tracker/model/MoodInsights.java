package com.example.maziyyah.mood_tracker.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// container for analytics results
public class MoodInsights {

    private String userId; // ID of the user insight belongs to
    private long epochDay;
    private LocalDate date; // Date of the insights
    private List<Integer> mostCommonMoodScore;  // maybe not needed..?
    private List<String> mostCommonMood; // Name of the most common mood
    private List<String> mostCommonMoodEmoji;
    private List<String> mostCommonMoodColors;
    private Map<Integer, Long> moodScoreDistribution; // Mood score distribution (eg {-2: 3, -1: 5, 0: 2})
    private Integer totalEntries; // Total number of mood entries
    private Map<String, Integer> moodsByTimeOfDay; // Moods logged by time of day
    
    // Add fields for future analytics (e.g., streaks, trends)
    // private Map<String, Integer> streaks;
    
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public List<String> getMostCommonMood() {
        return mostCommonMood;
    }
    public void setMostCommonMood(List<String> mostCommonMood) {
        this.mostCommonMood = mostCommonMood;
    }
    public List<Integer> getMostCommonMoodScore() {
        return mostCommonMoodScore;
    }
    public void setMostCommonMoodScore(List<Integer> mostCommonMoodScore) {
        this.mostCommonMoodScore = mostCommonMoodScore;
    }
    public Map<Integer, Long> getMoodScoreDistribution() {
        return moodScoreDistribution;
    }
    public void setMoodScoreDistribution(Map<Integer, Long> moodScoreDistribution) {
        this.moodScoreDistribution = moodScoreDistribution;
    }
    public Integer getTotalEntries() {
        return totalEntries;
    }
    public void setTotalEntries(Integer totalEntries) {
        this.totalEntries = totalEntries;
    }
    public Map<String, Integer> getMoodsByTimeOfDay() {
        return moodsByTimeOfDay;
    }
    public void setMoodsByTimeOfDay(Map<String, Integer> moodsByTimeOfDay) {
        this.moodsByTimeOfDay = moodsByTimeOfDay;
    }
    public long getEpochDay() {
        return epochDay;
    }
    public void setEpochDay(long epochDay) {
        this.epochDay = epochDay;
    }
    public List<String> getMostCommonMoodEmoji() {
        return mostCommonMoodEmoji;
    }
    public void setMostCommonMoodEmoji(List<String> mostCommonMoodEmoji) {
        this.mostCommonMoodEmoji = mostCommonMoodEmoji;
    }
    public List<String> getMostCommonMoodColors() {
        return mostCommonMoodColors;
    }
    public void setMostCommonMoodColors(List<String> mostCommonMoodColors) {
        this.mostCommonMoodColors = mostCommonMoodColors;
    }

    

    

    

    

    
}
