package com.example.maziyyah.mood_tracker.controller;

import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import com.example.maziyyah.mood_tracker.model.Update;
import com.example.maziyyah.mood_tracker.service.TelegramNotificationService;
import com.example.maziyyah.mood_tracker.service.WebhookService;

@RestController
public class WebhookController {

    private final WebhookService webhookService;
    private final TelegramNotificationService telegramNotificationService;
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);


    public WebhookController(WebhookService webhookService, TelegramNotificationService telegramNotificationService) {
        this.webhookService = webhookService;
        this.telegramNotificationService = telegramNotificationService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> receiveWebhook(@RequestBody String requestBody) {

        // parse the incoming JSON string
        try (JsonReader jsonReader = Json.createReader(new StringReader(requestBody))) {
            JsonObject jsonObject = jsonReader.readObject();
            Update update = webhookService.toUpdate(jsonObject);
            Integer updateId = jsonObject.getInt("update_id");
            System.out.println(updateId);

            // Check for duplicate update IDs using Redis
            boolean isDuplicate = webhookService.isDuplicateUpdate(updateId);
            if (isDuplicate) {
                System.out.println("Duplicate update_id detected, ignoring: " + updateId);
                return ResponseEntity.ok("ok");
            }

            // Store update_id as processed to avoid duplicate processing
            webhookService.markUpdateAsProcessed(updateId);

            // Extract message text and chat ID
            String messageText = update.getMessage_text();
            long chatId = update.getChat_id();

            logger.info("Processing /start command. Message text: {}", messageText);
            System.out.println("Received message: " + messageText);

            if (messageText.startsWith("/start")) {
                // extract the part after '/start'
                if (messageText.length() > 6) { // check if there is anything after '/start'
                    String token = messageText.substring(7).trim(); // Extract the part after '/start' and trim spaces
                    System.out.println("Extracted token: " + token);

                    if (token.length() == 6) { // 6-character tokens are "user" invites
                        if (webhookService.handleUserLinking(token, update)) {
                            System.out.println("Successfully linked Telegram userId and user account id.");
                            String successMessage = "Woohoo! Your account is linked and ready! 🎉\n"
                                    + "Sit back and relax—thoughtful reminders and uplifting encouragement will now come straight to your chat. 😊";
                            String chatIdString = String.valueOf(chatId);
                            try {
                                telegramNotificationService.sendTelegramNotification(chatIdString, successMessage);
                            } catch (Exception e) {
                                logger.error("Failed to send notification to chat ID {}: {}", chatId, e.getMessage(), e);
                            }
                            return ResponseEntity.ok("Successfully linked Telegram userId and user account id.");

                        }
                    } else if (token.length() == 8) { // 8-character tokens are "lovedone" invites
                        if (webhookService.handleLovedOneLinking(token, chatId)) {
                            System.out.println("Linked loved one successfully!");
                            String successMessage = "You’re now officially a loved one! ❤️\n"
                                    + "Thank you for being there to support someone who cares about you. You’re making a big difference already. 🌟";
                            String chatIdString = String.valueOf(chatId);
                            try {
                                telegramNotificationService.sendTelegramNotification(chatIdString, successMessage);
                            } catch (Exception e) {
                                logger.error("Failed to send notification to chat ID {}: {}", chatId, e.getMessage(), e);
                            }
                            return ResponseEntity.ok("Linked loved one successfully!");
                        }

                    } else {
                        System.out.println("Invalid token format");
                    }
                } else {
                    // Handle /start with no payload
                    System.out.println("No payload after /start");
                    return ResponseEntity.ok("Welcome to the bot! Please click a proper invite link.");
                }

            }

        }

        System.out.println("Received update: " + requestBody);

        return ResponseEntity.ok("ok");
    }

}
