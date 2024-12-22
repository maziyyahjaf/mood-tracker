package com.example.maziyyah.mood_tracker.model;

public enum Mood {
    AWFUL(-2, "Awful", "#D32F2F", "😔"),
    BAD(-1, "Bad", "#F4511E", "😕"),
    FINE(0, "Fine", "#FFCA28", "😐"),
    GOOD(1, "Good", "#81C784", "🙂"),
    AMAZING(2, "Amazing", "#4CAF50", "😄");


    private final int score;      // Mood score
    private final String label;  // Textual representation of the mood
    private final String color;  // Hex color for the mood
    private final String emoji;  // Emoji representing the mood

    Mood(int score, String label, String color, String emoji) {
        this.score = score;
        this.label = label;
        this.color = color;
        this.emoji = emoji;
    }

    public int getScore() {
        return score;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }

    public String getEmoji() {
        return emoji;
    }

    public static Mood getMoodForScore(int score) {
        for (Mood mood : Mood.values()) {
            if (mood.score == score) {
                return mood;
            }
        }
        throw new IllegalArgumentException("Invalid score: " + score);
    }
}
    

