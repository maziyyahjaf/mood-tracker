package com.example.maziyyah.mood_tracker.controller;

import java.io.StringReader;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import com.example.maziyyah.mood_tracker.model.Update;
import com.example.maziyyah.mood_tracker.service.WebhookService;

@RestController
public class WebhookController {

    private final WebhookService webhookService;

    // need to add method to programmatically set webhook

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
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
            System.out.println("Received message: " + messageText);

            if (messageText.startsWith("/start")) {
                // extract the part after '/start'
                if (messageText.length() > 6) { // check if ther is anything after '/start'
                    String token = messageText.substring(7).trim(); // Extract the part after '/start' and trim spaces
                    System.out.println("Extracted token: " + token);

                    if (token.length() == 6) { // 6-character tokens are "user" invites
                        if (webhookService.handleUserLinking(token, update)) {
                            System.out.println("Successfully linked Telegram userId and user account id.");
                            return ResponseEntity.ok("Successfully linked Telegram userId and user account id.");

                        }
                    } else if (token.length() == 8) { // 8-character tokens are "lovedone" invites
                        if (webhookService.handleLovedOneLinking(token, chatId)) {
                            System.out.println("Linked loved one successfully!");
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
