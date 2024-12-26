package com.example.maziyyah.mood_tracker.model;

import java.time.LocalDate;

public class DailyMoodSummary {
    
    private Long epochDay; // the day the summary correspons to
    private double averageMoodScore; // average mood score for the day
    private int numOfMoodEntries = 0; // number of entries for that day, default to zero
    private String summaryColor; // summary color of the day
    private LocalDate date; // human readable date
    private String emoji;


    public DailyMoodSummary() {
    }

    
    public DailyMoodSummary(long epochDay, double averageMoodScore, int numOfMoodEntries, String summaryColor) {
        this.epochDay = epochDay;
        this.averageMoodScore = averageMoodScore;
        this.numOfMoodEntries = numOfMoodEntries;
        this.summaryColor = summaryColor;
        this.date = LocalDate.ofEpochDay(epochDay); // Convert epochDay to ISO date (e.g., "2024-12-19")
    }


    public double getAverageMoodScore() {
        return averageMoodScore;
    }

    public void setAverageMoodScore(double averageMoodScore) {
        this.averageMoodScore = averageMoodScore;
    }

    public int getNumOfMoodEntries() {
        return numOfMoodEntries;
    }

    public void setNumOfMoodEntries(int numOfMoodEntries) {
        this.numOfMoodEntries = numOfMoodEntries;
    }

    public String getSummaryColor() {
        return summaryColor;
    }

    public void setSummaryColor(String summaryColor) {
        this.summaryColor = summaryColor;
    }

    public Long getEpochDay() {
        return epochDay;
    }

    public void setEpochDay(Long epochDay) {
        this.epochDay = epochDay;
    }


    public LocalDate getDate() {
        return date;
    }


    public void setDate(LocalDate date) {
        this.date = date;
    }


    public String getEmoji() {
        return emoji;
    }


    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    


   

    

    


    
}
