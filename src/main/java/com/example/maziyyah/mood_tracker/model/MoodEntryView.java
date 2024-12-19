package com.example.maziyyah.mood_tracker.model;

import java.util.Date;

public class MoodEntryView {

    private String moodEntryId;
    private Date timestamp;
    private Integer moodScore;
    private String color;
    private String note;

    public MoodEntryView() {
    }

    public MoodEntryView(String moodEntryId, Date timestamp, Integer moodScore, String color, String note) {
        this.moodEntryId = moodEntryId;
        this.timestamp = timestamp;
        this.moodScore = moodScore;
        this.color = color;
        this.note = note;
    }


    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date timestamp) {
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

  

    

    
    
}
