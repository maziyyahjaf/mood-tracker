package com.example.maziyyah.mood_tracker.service;

import java.net.URI;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationService.class);

    public TelegramNotificationService(TelegramNotificationRepo telegramNotificationRepo) {
        this.telegramNotificationRepo = telegramNotificationRepo;
    }

    
    // need the chatid of user
    // need the chatid of lovedones

    public void sendMessage(String userId) {

        Optional<String> userChatIdOpt = getUserChatId(userId);
        if (userChatIdOpt.isEmpty()) {
            logger.warn("Cannot send message because user {} has no Telegram chat ID linked.", userId);
            return;
        }

        String userChatId = userChatIdOpt.get();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/json");

        String message = "Things will be okay";

        JsonObject jsonObject = Json.createObjectBuilder()
                                .add("chat_id", userChatId)
                                .add("text", message)
                                .build();

        RequestEntity<String> req = RequestEntity
                                    .post(URI.create(buildSendMessageUrl()))
                                    .headers(headers)
                                    .body(jsonObject.toString());
        try {
            ResponseEntity<String> response = restTemplate.exchange(req, String.class);
            logger.info("Response: Status={}, Body={}", response.getStatusCode(), response.getBody());
            System.out.println(response);
        } catch (Exception e)    {
            logger.error("Failed to send Telegram message to userId: {}. Error: {}", userId, e.getMessage(), e);

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
}
