package com.example.maziyyah.mood_tracker.model;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

public class MoodEntryView {

    private String moodEntryId;
    private Date timestamp;
    private ZonedDateTime zonedDateTime;
    private Integer moodScore;
    private String color;
    private String note;
    public String emoji;
    public List<String> tags;

    public MoodEntryView() {
    }

    public MoodEntryView(String moodEntryId, Date timestamp, Integer moodScore, String color, String note, List<String> tags) {
        this.moodEntryId = moodEntryId;
        this.timestamp = timestamp;
        this.moodScore = moodScore;
        this.color = color;
        this.note = note;
        this.tags = tags;
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

    public ZonedDateTime getZonedDateTime() {
        return zonedDateTime;
    }

    public void setZonedDateTime(ZonedDateTime zonedDateTime) {
        this.zonedDateTime = zonedDateTime;
    }

    

    
    

  

    

    
    
}
