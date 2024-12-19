package com.example.maziyyah.mood_tracker.model;

public enum MoodEmoji {
    AWFUL(-Double.MAX_VALUE, -2, "😢"),
    BAD(-2, -1, "😞"),
    NEUTRAL(-1, 0, "😐"),
    GOOD(0, 1, "😊"),
    AMAZING(1, Double.MAX_VALUE, "😁");

    private final double min; // Inclusive lower bound
    private final double max; // Exclusive upper bound
    private final String emoji; // Emoji for this mood

    // Constructor
    MoodEmoji(double min, double max, String emoji) {
        this.min = min;
        this.max = max;
        this.emoji = emoji;
    }

    // Static method to return the emoji for a given averageScore
    public static String getEmojiFor(double averageScore) {
        for (MoodEmoji moodEmoji : MoodEmoji.values()) {
            if (averageScore > moodEmoji.min && averageScore <= moodEmoji.max) {
                return moodEmoji.emoji; // return the emoji if score fits range
            }
        }
        return "unknown";

    }
}
