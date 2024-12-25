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
import com.example.maziyyah.mood_tracker.model.LovedOneDTO;
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

    public boolean removeLovedOne(String userId, String lovedOneId) {
        return lovedOneRepository.removeLovedOne(userId, lovedOneId);
    }

    // invite token ttl?

    // when i remove loved one..need to remove loved one data too
    public void deleteLovedOneData(String userId, String lovedOneId) {
        logger.info("Deleting loved one with id: {}", lovedOneId);
        if (removeLovedOne(userId, lovedOneId)) {
            lovedOneRepository.deleteLovedOneData(lovedOneId);
            lovedOneRepository.deleteLovedOneInviteToken(lovedOneId);
        }
        logger.error("Error deleting loved one with id: {}", lovedOneId);
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

    public List<LovedOneDTO> getAllLovedOnes(Optional<List<String>> lovedOnesId) {
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

    public LovedOneDTO getLovedOneData(String lovedOneId) {
        logger.info("Fetching data for lovedOneId: {}", lovedOneId);

        // Retrieve the hash data for the loved one
        Map<Object, Object> lovedOneHash = lovedOneRepository.getLovedOneData(lovedOneId);
        if (lovedOneHash == null || lovedOneHash.isEmpty()) {
            logger.error("No data found for lovedOneId: {}", lovedOneId);
            throw new IllegalArgumentException("No data found for lovedOneId: " + lovedOneId);
        }

        logger.debug("Data retrieved for lovedOneId: {}", lovedOneId);

        LovedOneDTO lovedOne = new LovedOneDTO();
        lovedOne.setLovedOneId(getStringValue(lovedOneHash, "lovedOneId"));
        lovedOne.setName(getStringValue(lovedOneHash, "name"));
        lovedOne.setContact(getStringValue(lovedOneHash, "contact"));
        lovedOne.setRelationship(getStringValue(lovedOneHash, "relationship"));

        // Determine the status of the loved one
        String chatIdStr = getStringValue(lovedOneHash, Constant.LOVED_ONE_CHAT_ID_FIELD);
        if (chatIdStr != null && !chatIdStr.isEmpty()) {
            lovedOne.setStatus("Linked ✅");
        } else {
            lovedOne.setStatus(determineInviteStatus(lovedOneId));
        }

        logger.info("Successfully created LovedOneDTO for lovedOneId: {}", lovedOneId);
        return lovedOne;
    }

    private String determineInviteStatus(String lovedOneId) {
        logger.info("Determining invite status for lovedOneId: {}", lovedOneId);

        // Retrieve the invite token for the loved one
        Optional<String> inviteToken = getLovedOneIdInviteToken(lovedOneId);

        if (inviteToken.isEmpty()) {
            logger.warn("No invite token found for lovedOneId: {}", lovedOneId);
            return "Not invited";
        }

        String inviteTokenString = inviteToken.get();
        Map<Object, Object> inviteData = lovedOneRepository.getInviteData(inviteTokenString);

        if (inviteData != null && !inviteData.isEmpty()) {
            long expiresAt = safeParseLong(inviteData.get("expiresAt"));
            if (System.currentTimeMillis() < expiresAt) {
                logger.info("Invite for lovedOneId: {} is still valid", lovedOneId);
                return "Waiting for Join";
            } else {
                logger.warn("Invite for lovedOneId: {} has expired", lovedOneId);
                return "Invite Expired";
            }
        }

        logger.error("Invite data for lovedOneId: {} is missing or corrupted", lovedOneId);
        return "Not invited";
    }

    public String resendInvite(String userId, String lovedOneId) {
        // Retrieve the invite token for the loved one
        Optional<String> inviteToken = getLovedOneIdInviteToken(lovedOneId);
        if (inviteToken.isPresent()) {
            String inviteTokenString = inviteToken.get();
            Map<Object, Object> inviteData = lovedOneRepository.getInviteData(inviteTokenString);
            if (inviteData != null && !inviteData.isEmpty()) {
                long expiresAt = safeParseLong(inviteData.get("expiresAt"));
                if (System.currentTimeMillis() < expiresAt) {
                    // invite token is still valid, reuse it
                    // Log token reuse
                    logger.info("Reusing valid invite token for lovedOneId: {}", lovedOneId);
                    return inviteTokenString;
                }
            }
        }

        // generate and log new token
        return generateNewInviteToken(userId, lovedOneId);

    }

    public String generateNewInviteLink(String newInviteToken) {
        String newInviteLink = appInviteUrl + newInviteToken;
        return newInviteLink;
    }

    private String generateNewInviteToken(String userId, String lovedOneId) {
        String newInviteToken = generateInviteToken();
        lovedOneRepository.generateNewInviteLink(userId, lovedOneId, newInviteToken);
        // Log token generation
        logger.info("Generated new invite token for lovedOneId: {}", lovedOneId);
        return newInviteToken;
    }

    private String getStringValue(Map<Object, Object> map, String key) {
        if (map == null) {
            return "";
        }
        Object value = map.get(key);
        return (value != null) ? value.toString() : "";
    }

    private long safeParseLong(Object value) {
        try {
            return value != null ? Long.parseLong(value.toString()) : 0L;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public Optional<String> getLovedOneIdInviteToken(String lovedOneId) {
        Object lovedOneIdInviteToken = lovedOneRepository.getLovedOneIdInviteToken(lovedOneId);
        return Optional.ofNullable(lovedOneIdInviteToken).map(Object::toString);
    }

    public boolean hasLovedOneLinkedTelegramChatId(String lovedOneId) {
        Object chatId = lovedOneRepository.hasLovedOneLinkedTelegramChatId(lovedOneId);
        // Return false if chatId is null or its string representation is empty
        return chatId != null && !chatId.toString().isEmpty();
    }

}
