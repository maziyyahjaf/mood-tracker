package com.example.maziyyah.mood_tracker.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.maziyyah.mood_tracker.service.TelegramNotificationService;

@RestController
public class TelegramNotificationController {

    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationController.class);

    private final TelegramNotificationService telegramNotificationService;

    public TelegramNotificationController(TelegramNotificationService telegramNotificationService) {
        this.telegramNotificationService = telegramNotificationService;
    }

    @GetMapping("/send-notification")
    public String sendManualNotification(@RequestParam("userId") String userId, @RequestParam("epochDay") long epochDay) {
        logger.info("Received request to send manual Telegram notification for userId: {}", userId);
        try {
            telegramNotificationService.sendEncouragementMessage(userId, epochDay);
            return "Notification sent succesfully to userId: " + userId;
        } catch (Exception e) {
            logger.error("Failed to send notification for userId: {}. Error: {}", userId, e.getMessage(), e);
            return "Failed to send notification. Check logs for more details";
        }
    }
    
    // use this to test the llm service 
}
