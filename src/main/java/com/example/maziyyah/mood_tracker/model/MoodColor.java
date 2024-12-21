package com.example.maziyyah.mood_tracker.model;

public enum MoodColor {
    DARK_RED(-Double.MAX_VALUE, -2, "#D32F2F"),
    ORANGE_RED(-2, -1, "#F4511E"),
    GOLDEN_YELLOW(-1, 0, "#FFCA28"),
    LIGHT_GREEN(0, 1, "#81C784"),
    DARK_GREEN(1, Double.MAX_VALUE, "#4CAF50");

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
