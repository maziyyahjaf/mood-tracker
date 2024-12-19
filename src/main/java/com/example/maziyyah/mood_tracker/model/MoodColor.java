package com.example.maziyyah.mood_tracker.model;

public enum MoodColor {
    DARK_RED(-Double.MAX_VALUE, -2, "#8B0000"),
    ORANGE_RED(-2, -1, "#FF4500"),
    GOLDEN_YELLOW(-1, 0, "#FFD700"),
    LIGHT_GREEN(0, 1, "#90EE90"),
    DARK_GREEN(1, Double.MAX_VALUE, "#006400");

    private final double min;  // Lower bound for this mood range
    private final double max;  // Upper bound for this mood range
    private final String color; // Color to display for this range

    // Constructor for each mood color
    MoodColor(double min, double max, String color) {
        this.min = min;
        this.max = max;
        this.color = color;
    }

    // Static method to return the color for a given averageScore
    public static String getColorFor(double averageScore) {
        for (MoodColor moodColor : MoodColor.values()) {
            if (averageScore > moodColor.min && averageScore <= moodColor.max) {
                return moodColor.color; // Return the color if score fits the range
            }
        }
        return "unknown"; // Fallback if no range matches
    }
}
