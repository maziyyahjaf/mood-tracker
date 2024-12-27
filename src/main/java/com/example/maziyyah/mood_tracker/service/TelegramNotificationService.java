package com.example.maziyyah.mood_tracker.service;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.maziyyah.mood_tracker.model.MoodEntry;
import com.example.maziyyah.mood_tracker.repository.TelegramNotificationRepo;

import jakarta.json.Json;
import jakarta.json.JsonObject;

@Service
public class TelegramNotificationService {

    @Value("${telegram.bot.token}")
    private String telegramBotToken;

    @Value("${telegram.bot.message.url}")
    private String telegramBotMessageUrl;

    RestTemplate restTemplate = new RestTemplate();

    private final TelegramNotificationRepo telegramNotificationRepo;
    private final MoodInsightsService moodInsightsService;
    private final ExternalLLMService externalLLMService;
    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationService.class);

    public TelegramNotificationService(TelegramNotificationRepo telegramNotificationRepo,
            MoodInsightsService moodInsightsService, ExternalLLMService externalLLMService) {
        this.telegramNotificationRepo = telegramNotificationRepo;
        this.moodInsightsService = moodInsightsService;
        this.externalLLMService = externalLLMService;
    }

    // need the chatid of user
    // need the chatid of lovedones

    public void sendEncouragementMessage(String userId, long epochDay) {

        Optional<String> userChatIdOpt = getUserChatId(userId);
        if (userChatIdOpt.isEmpty()) {
            logger.warn("Cannot send message because user {} has no Telegram chat ID linked.", userId);
            return;
        }

        // fetch bad mood entries using MoodInsightsService
        List<MoodEntry> badMoodEntries = moodInsightsService.getBadMoodEntriesForDay(userId, epochDay);
        if (badMoodEntries.isEmpty()) {
            logger.info("No bad mood entries found for user {}. Skipping encouragement message.", userId);
            return;
        }

        // Summarize mood insights
        String summary = summarizeBadMoodEntries(badMoodEntries);

        // craft the dynamic messages -> either stored in redis or from API call
        String dynamicMessage = craftEncouragementMessage(summary, userId);

        // Prepare the Telegram API request
        String userChatId = userChatIdOpt.get();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/json");

        JsonObject jsonObject = Json.createObjectBuilder()
                .add("chat_id", userChatId)
                .add("text", dynamicMessage)
                .build();

        sendTelegramNotificationRequest(jsonObject);

    }

    public void sendScheduledEncouragementMessage(String userId, String message) {
        Optional<String> userChatIdOpt = getUserChatId(userId);
        if (userChatIdOpt.isEmpty()) {
            logger.warn("Cannot send message because user {} has no Telegram chat ID linked.", userId);
            return;
        }

        // Prepare the Telegram API request
        String userChatId = userChatIdOpt.get();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/json");

        JsonObject jsonObject = Json.createObjectBuilder()
                .add("chat_id", userChatId)
                .add("text", message)
                .build();

        sendTelegramNotificationRequest(jsonObject);

    }

    public void sendNotificationToLovedOnes(String userId, int currentStreak) {

        Optional<List<String>> lovedOnesIdsOpt = getAllLovedOnes(userId);
        if (lovedOnesIdsOpt.isEmpty()) {
            logger.warn("Cannot send message because user {} has no loved ones linked.", userId);
            return;
        }

        List<String> lovedOnesIds = lovedOnesIdsOpt.get();
        for (String lovedOneId : lovedOnesIds) {
            Optional<String> lovedOneIdChatIdOpt = getLovedOnesChatId(lovedOneId);
            if (lovedOneIdChatIdOpt.isEmpty()) {
                logger.info("Loved One with ID {} did not link their Telegram account", lovedOneId);
                continue;
            }
            String lovedOneIdChatId = lovedOneIdChatIdOpt.get();
            // get user details
            String userName = getUserName(userId);
            // ** maybe make it dynamic as well?
            String message = userName + " has had a " + currentStreak + " tough days in a row. Send them some love ❤.";
            sendTelegramNotification(lovedOneIdChatId, message);

        }

    }

    public void sendTelegramNotification(String telegramChatId, String message) {

        JsonObject jsonObject = Json.createObjectBuilder()
                .add("chat_id", telegramChatId)
                .add("text", message)
                .build();

        sendTelegramNotificationRequest(jsonObject);
    }

    public void sendTelegramNotificationRequest(JsonObject jsonObject) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/json");

        RequestEntity<String> req = RequestEntity
                .post(URI.create(buildSendMessageUrl()))
                .headers(headers)
                .body(jsonObject.toString());

        try {
            ResponseEntity<String> response = restTemplate.exchange(req, String.class);
            logger.info("Response: Status={}, Body={}", response.getStatusCode(), response.getBody());
            System.out.println(response);
        } catch (Exception e) {
            logger.error("Failed to send Telegram message. Error: {}", e.getMessage(), e);

        }

    }

    public String buildSendMessageUrl() {
        return UriComponentsBuilder.fromUriString(telegramBotMessageUrl)
                .pathSegment("bot" + telegramBotToken)
                .pathSegment("sendMessage")
                .toUriString();
    }

    public Optional<String> getUserChatId(String userId) {
        return Optional.ofNullable(telegramNotificationRepo.getUserChatId(userId))
                .map(Object::toString)
                .map(chatId -> {
                    logger.info("User with ID {} has a linked Telegram chat Id: {}", userId, chatId);
                    return chatId;
                })
                .or(() -> {
                    logger.info("User with ID {} did not link their Telegram account", userId);
                    return Optional.empty();
                });
    }

    public Optional<List<String>> getAllLovedOnes(String userId) {
        Set<Object> retrievedData = telegramNotificationRepo.getAllLovedOnes(userId);

        if (retrievedData == null || retrievedData.isEmpty()) {
            logger.info("No linked loved ones");
            return Optional.empty();
            // maybe can send a message reminder to add a loved one?
        }

        List<String> lovedOneIds = retrievedData.stream()
                .map(Object::toString) // convert each item to String
                .collect(Collectors.toList());

        return Optional.of(lovedOneIds);
    }

    public Optional<String> getLovedOnesChatId(String lovedOneId) {
        return Optional.ofNullable(telegramNotificationRepo.getLovedOneChatId(lovedOneId))
                .map(Object::toString)
                .map(chatId -> {
                    logger.info("Loved One with ID {} has a linked Telegram chat Id: {}", lovedOneId, chatId);
                    return chatId;
                })
                .or(() -> {
                    logger.info("Loved One with ID {} did not link their Telegram account", lovedOneId);
                    return Optional.empty();
                });
    }

    public String getUserName(String userId) {
        return Optional.ofNullable(telegramNotificationRepo.getUserName(userId))
                .map(Object::toString)
                .orElse("Unknown User");
    }

    public String summarizeBadMoodEntries(List<MoodEntry> badMoodEntries) {
        StringBuilder summary = new StringBuilder("Recent bad mood patterns: \n");
        for (MoodEntry entry : badMoodEntries) {
            summary.append("-").append(entry.getNote())
                    .append(" (Tags: ").append(String.join(", ", entry.getTags()))
                    .append(")\n");
        }
        System.out.println("Generated Mood Summary: " + summary);
        return summary.toString();
    }

    public String craftEncouragementMessage(String summary, String userId) {
        System.out.println("Received summary: " + summary);
        System.out.println("Received userId: " + userId);
        String userName = getUserName(userId);
        System.out.println("Resolved userName: " + userName);
        return externalLLMService.generateMessage(summary, userName);

    }
}
