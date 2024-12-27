package com.example.maziyyah.mood_tracker.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.maziyyah.mood_tracker.constant.Constant;
import com.example.maziyyah.mood_tracker.model.Mood;
import com.example.maziyyah.mood_tracker.model.MoodEntry;
import com.example.maziyyah.mood_tracker.model.User;
import com.example.maziyyah.mood_tracker.repository.EncouragementMessageRepo;

@Service
public class EncouragementMessageService {

    // this will generate the prompt to send to external LLM
    private final EncouragementMessageRepo encouragementMessageRepo;
    private final UserService userService;
    private final MoodTrackerService moodTrackerService;
    private final ExternalLLMService externalLLMService;

    public EncouragementMessageService(EncouragementMessageRepo encouragementMessageRepo, UserService userService,
            MoodTrackerService moodTrackerService, ExternalLLMService externalLLMService) {
        this.encouragementMessageRepo = encouragementMessageRepo;
        this.userService = userService;
        this.moodTrackerService = moodTrackerService;
        this.externalLLMService = externalLLMService;
    }

    // i need the long epochDay so i can fetch the mood entries logged for the day
    // i need to format the entries to build the prompt to send to Gemini
    // then i need to save the message received from Gemini into redis with the
    // proper key

    // have a generatePrompt method
    public void generateAndSaveMessage(String userId) {

        // from user id get the user hash
        User user = userService.getUserDetailsByUserId(userId);

        // get user time zone
        String timeZone = user.getTimeZone();
        LocalDate yesterday = ZonedDateTime.now(ZoneId.of(timeZone)).toLocalDate().minusDays(1);
        LocalDate currentDate = ZonedDateTime.now(ZoneId.of(timeZone)).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentDateString = currentDate.format(formatter);

        // determine yesterday epoch day
        long yesterdayEpochDay = yesterday.toEpochDay();

        // fetch mood entries for yesterday
        List<MoodEntry> moodEntries = moodTrackerService.getMoodEntriesForDay(userId, yesterdayEpochDay);

        String context = generateContext(moodEntries, timeZone, currentDateString);
        String dynamicMessage = craftEncouragementMessage(context, userId);
        saveMessageToRedis(userId, dynamicMessage);

    }

    public String generateContext(List<MoodEntry> entries, String timeZone, String currentDateString) {
        if (entries.isEmpty()) {
            return "Good Morning! It's a fresh new day. Remember to take some time for yourself today.";
        }

        entries.sort((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()));

        StringBuilder context = new StringBuilder();
        context.append("Here are the user's most recent mood logs:\n");

        for (int i = 0; i < Math.min(entries.size(), Constant.MOOD_ENTRY_PROMPT_LIMIT); i++) {
            MoodEntry entry = entries.get(i);
            try {
                String formattedTime = epochMilliToFormattedDateTime(timeZone, entry.getTimestamp());
                context.append(i + 1).append(". [").append(formattedTime).append("] ")
                        .append("Mood: \"").append(Mood.getMoodForScore(entry.getMoodScore())).append("\" - ")
                        .append("\"").append(entry.getNote()).append("\" ")
                        .append("(Tags: ").append(entry.getTags() != null ? String.join(", ", entry.getTags()) : "None")
                        .append(")\n");
            } catch (Exception e) {
                context.append("- Error formatting time for entry with note: ").append(entry.getNote()).append("\n");
                // Log exception for debugging
                e.printStackTrace();
            }
        }

        context.append("\n Current date and time: [").append(currentDateString).append("]");
        return context.toString();

    }

    public String epochMilliToFormattedDateTime(String timeZone, long epochMilli) {

        ZoneId userZoneId = ZoneId.of(timeZone);

        // Convert epoch milliseconds to LocalDateTime
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), userZoneId);

        // Define the formatter for date and time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = dateTime.format(formatter);

        return formattedDateTime;
    }

    public String getUserName(String userId) {
        return Optional.ofNullable(encouragementMessageRepo.getUserName(userId))
                .map(Object::toString)
                .orElse("Unknown User");
    }

    public String craftEncouragementMessage(String context, String userId) {
        String userName = getUserName(userId);
        return externalLLMService.generateMessage(context, userName);
    }

    public void saveMessageToRedis(String userId, String message) {
        encouragementMessageRepo.saveMessageToRedis(userId, message);
    }

    public String getMessageFromRedis(String userId) {
        return encouragementMessageRepo.getMessageFromRedis(userId);
    }

}
