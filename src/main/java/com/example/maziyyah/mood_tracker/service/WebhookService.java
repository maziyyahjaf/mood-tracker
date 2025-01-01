package com.example.maziyyah.mood_tracker.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.maziyyah.mood_tracker.model.Update;
import com.example.maziyyah.mood_tracker.repository.WebhookRepository;


import jakarta.json.JsonObject;

@Service
public class WebhookService {

    private final WebhookRepository webhookRepository;

    public WebhookService(WebhookRepository webhookRepository) {
        this.webhookRepository = webhookRepository;
    }

    public Boolean isDuplicateUpdate(Integer updateId) {
        return webhookRepository.isDuplicateUpdate(updateId);
    }

    public void markUpdateAsProcessed(Integer updateId) {
        webhookRepository.markUpdateAsProcessed(updateId);
    }

    public Optional<String> checkIfLinkingCodeExists(String linkingCode) {
        Object userIdObj = webhookRepository.checkIfLinkingCodeExist(linkingCode);

        if (userIdObj == null) {
            return Optional.empty();
        }

        String userId = userIdObj.toString();
        return Optional.of(userId);

    }

    public Long processUpdateId(JsonObject jsonObject) {
        if (jsonObject == null || !jsonObject.containsKey("update_id")) {
            throw new IllegalArgumentException("Invalid JSON: Missing 'update_id'");
        }
    
        // Extract the update_id
        return jsonObject.getJsonNumber("update_id").longValue();
    }

    public Update toUpdate(JsonObject jsonObject) {
        if (jsonObject == null || !jsonObject.containsKey("message")) {
            throw new IllegalArgumentException("Invalid JSON: Missing 'message' object");
        }
        JsonObject message = jsonObject.getJsonObject("message");

        // Extract message text
        String message_text = message.containsKey("text") ? message.getString("text") : null;

        // Extract sender's details
        JsonObject from = message.getJsonObject("from");
        Long from_id = from != null && from.containsKey("id") ? from.getJsonNumber("id").longValue() : null;

        // Extract chat details
        JsonObject chat = message.getJsonObject("chat");
        Long chat_id = chat != null && chat.containsKey("id") ? chat.getJsonNumber("id").longValue() : null;

        if (chat_id == null || from_id == null || message_text == null) {
            throw new IllegalArgumentException("Invalid JSON: Missing required fields");
        }

        Update update = new Update(chat_id, from_id, message_text);

        return update;
    }

    // need to extract linking code from message text
    public String linkingCode(Update update) {
        String linkingCode = null;
        String message_text = update.getMessage_text();

        if (message_text.startsWith("/start ")) {
            linkingCode = message_text.replace("/start ", "").trim();
        }

        return linkingCode;
    }

    public Boolean handleLovedOneLinking(String inviteToken, long chatId) {
        Optional<Map<Object, Object>> inviteData = checkIfInviteTokenExists(inviteToken);
        if (inviteData.isPresent()) {
            String lovedOneId = Optional.ofNullable(inviteData.get().get("lovedOneId"))
                    .map(Object::toString)
                    .orElseThrow(() -> new IllegalArgumentException("invite data does not exist"));
            String chatIdString = String.valueOf(chatId);
            webhookRepository.linkLovedOneIdToChatId(lovedOneId, chatIdString);
            webhookRepository.deleteInviteToken(inviteToken);
            return true;
        }

        return false;
    }

    public Optional<Map<Object, Object>> checkIfInviteTokenExists(String inviteToken) {
        Map<Object, Object> retrievedInviteData = webhookRepository.checkIfInviteTokenExists(inviteToken);

        if (retrievedInviteData == null) {
            return Optional.empty();
        }

        return Optional.of(retrievedInviteData);
    }

    public Boolean handleUserLinking(String linkingCode, Update update) {
        Optional<String> userId = checkIfLinkingCodeExists(linkingCode);

        if (userId.isEmpty()) {
            System.out.println("invalid linking code");
            return false;

        }

        String userIdValue = userId.get();
        linkTelegramUserIdToAppUserId(update, userIdValue);
        linkUserIdToChatId(userIdValue, update);
        System.out.println(
                "Successfully linked Telegram userId " + update.getFrom_id() + " with userId " + userIdValue);

        return true;
    }

    public void linkTelegramUserIdToAppUserId(Update update, String userId) {
        String from_id = String.valueOf(update.getFrom_id());
        webhookRepository.linkTelegramUserIdToAppUserId(from_id, userId);
    }

    public void linkUserIdToChatId(String userId, Update update) {
        String chat_id = String.valueOf(update.getChat_id());
        webhookRepository.linkUserIdToChatId(userId, chat_id);
    }

}
