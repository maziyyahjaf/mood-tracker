package com.example.maziyyah.mood_tracker.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.maziyyah.mood_tracker.repository.TelegramNotificationRepo;

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


    public String buildSendMessageUrl() {
        return UriComponentsBuilder.fromUriString(telegramBotMessageUrl)
                                    .pathSegment("bot" + telegramBotToken, "sendMessage")
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
