package com.example.maziyyah.mood_tracker.model;

public class TimeZoneOption {
    private String zoneId;      // Raw zone ID (e.g., "America/New_York")
    private String displayName; // Formatted string (e.g., "(UTC-05:00) America/New_York")

    public TimeZoneOption(String zoneId, String displayName) {
        this.zoneId = zoneId;
        this.displayName = displayName;
    }

    public String getZoneId() {
        return zoneId;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
