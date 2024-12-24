package com.example.maziyyah.mood_tracker.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.maziyyah.mood_tracker.constant.Constant;
import com.example.maziyyah.mood_tracker.model.LovedOne;
import com.example.maziyyah.mood_tracker.repository.LovedOneRepository;

@Service
public class LovedOneService {

    @Value("${app.invite.url}")
    private String appInviteUrl;
    private final LovedOneRepository lovedOneRepository;
    private static final Logger logger = LoggerFactory.getLogger(LovedOneService.class);

    public LovedOneService(LovedOneRepository lovedOneRepository) {
        this.lovedOneRepository = lovedOneRepository;
    }

    public String generateInviteLink(String userId, LovedOne lovedOne) {
        String inviteToken = generateInviteToken();
        lovedOneRepository.generateInviteLink(userId, lovedOne, inviteToken);
        String inviteLink = appInviteUrl + inviteToken;

        return inviteLink;

    }

    public String generateInviteToken() {
        String inviteToken = UUID.randomUUID().toString().substring(0, 8);
        return inviteToken;
    }

    public void addLovedOne(String userId, LovedOne lovedOne) {
        lovedOneRepository.addLovedOne(userId, lovedOne);
    }

    public Optional<List<String>> getAllAddedLovedOnesId(String userId) {
        // Retrieve the set of loved ones IDs from the repository
        Set<Object> lovedOnesIdObjs = lovedOneRepository.getAllLovedOnes(userId);

        // If null or empty, return Optional.empty()
        if (lovedOnesIdObjs == null || lovedOnesIdObjs.isEmpty()) {
            return Optional.empty();
        }

        // Convert the set of objects to a list of strings
        List<String> addedLovedOnesId = lovedOnesIdObjs.stream()
                .map(Object::toString)
                .toList();

        // Wrap the list in an Optional and return
        return Optional.of(addedLovedOnesId);
    }

    public List<LovedOne> getAllLovedOnes(Optional<List<String>> lovedOnesId) {
        // If the Optional is empty or contains an empty list, return an empty list
        return lovedOnesId.orElseGet(Collections::emptyList)
                .stream()
                .map(id -> {
                    try {
                        return getLovedOneData(id);
                    } catch (IllegalArgumentException e) {
                        logger.error("Error retrieving loved one data for ID: {}", id, e);
                        return null; // Graceful handling
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public LovedOne getLovedOneData(String lovedOneId) {
        // Retrieve the hash data for the loved one
        Map<Object, Object> lovedOneHash = lovedOneRepository.getLovedOneData(lovedOneId);
        if (lovedOneHash == null || lovedOneHash.isEmpty()) {
            throw new IllegalArgumentException("No data found for lovedOneId: " + lovedOneId);
        }

        // Create and populate the LovedOne object
        LovedOne lovedOne = new LovedOne();
        lovedOne.setLoveOneId(getStringValue(lovedOneHash, "lovedOneId"));
        lovedOne.setName(getStringValue(lovedOneHash, "name"));
        lovedOne.setContact(getStringValue(lovedOneHash, "contact"));
        lovedOne.setRelationship(getStringValue(lovedOneHash, "relationship"));

        // Check if Telegram chat ID is linked
        String chatIdStr = getStringValue(lovedOneHash, Constant.LOVED_ONE_CHAT_ID_FIELD);
        if (!chatIdStr.isEmpty()) {
            lovedOne.setChatId(Long.parseLong(chatIdStr));
        }

        lovedOne.setTelegramStatus(getStringValue(lovedOneHash, "telegram_status"));

        return lovedOne;
    }

    /**
     * Utility method to safely retrieve a String value from a Map.
     * 
     * @param map The map to retrieve the value from.
     * @param key The key for the desired value.
     * @return The value as a String, or an empty string if the key is missing or
     *         the value is null.
     */
    private String getStringValue(Map<Object, Object> map, String key) {
        if (map == null) {
            return "";
        }
        Object value = map.get(key);
        return (value != null) ? value.toString() : "";
    }

    public boolean hasLovedOneLinkedTelegramChatId(String lovedOneId) {
        Object chatId = lovedOneRepository.hasLovedOneLinkedTelegramChatId(lovedOneId);
        // Return false if chatId is null or its string representation is empty
        return chatId != null && !chatId.toString().isEmpty();
    }

}
