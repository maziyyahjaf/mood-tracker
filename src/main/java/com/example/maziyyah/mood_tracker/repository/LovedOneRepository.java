package com.example.maziyyah.mood_tracker.repository;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.example.maziyyah.mood_tracker.constant.Constant;
import com.example.maziyyah.mood_tracker.model.LovedOne;
import com.example.maziyyah.mood_tracker.util.Utils;

@Repository
public class LovedOneRepository {

    @Qualifier(Utils.template01)
    private final RedisTemplate<String, Object> template;

    public LovedOneRepository(@Qualifier(Utils.template01) RedisTemplate<String, Object> template) {
        this.template = template;
    }

    public void generateInviteLink(String userId, LovedOne lovedOne, String inviteToken) {
        String inviteKey = Constant.INVITE_KEY_PREFIX + inviteToken;
        String lovedOneKey = Constant.LOVED_ONE_KEY_PREFIX + lovedOne.getLoveOneId();
        String inviteTokenKey = Constant.LOVED_ONE_KEY_PREFIX + lovedOne.getLoveOneId() + ":invite_token";

        Map<String, String> lovedOneData = new HashMap<>();
        lovedOneData.put("lovedOneId", lovedOne.getLoveOneId());
        lovedOneData.put("name", lovedOne.getName());
        lovedOneData.put("contact", lovedOne.getContact());
        lovedOneData.put("relationship", lovedOne.getRelationship());

        Map<String, String> inviteData = new HashMap<>();
        inviteData.put("userId", userId);
        inviteData.put("lovedOneId", lovedOne.getLoveOneId());
        inviteData.put("expiresAt", String.valueOf(Instant.now().plusSeconds(604800).toEpochMilli())); // 7 day expiry

        template.opsForHash().putAll(lovedOneKey, lovedOneData);
        template.opsForHash().putAll(inviteKey, inviteData);
        template.opsForValue().set(inviteTokenKey, inviteToken);
    }

    public void generateNewInviteLink(String userId, String lovedOneId, String newInviteToken) {
        String inviteKey = Constant.INVITE_KEY_PREFIX + newInviteToken;
        String inviteTokenKey = Constant.LOVED_ONE_KEY_PREFIX + lovedOneId + ":invite_token";

        Map<String, String> inviteData = new HashMap<>();
        inviteData.put("userId", userId);
        inviteData.put("lovedOneId", lovedOneId);
        inviteData.put("expiresAt", String.valueOf(Instant.now().plusSeconds(604800).toEpochMilli())); // 7 day expiry

        template.opsForHash().putAll(inviteKey, inviteData);
        template.opsForValue().set(inviteTokenKey, newInviteToken);

    }

    public void addLovedOne(String userId, LovedOne lovedOne) {
        String lovedOneId = lovedOne.getLoveOneId();
        String userLovedOnesKey = Constant.USER_KEY_PREFIX + userId + ":loved_ones";
        template.opsForSet().add(userLovedOnesKey, lovedOneId);
    }

    public boolean removeLovedOne(String userId, String lovedOneId) {
        String userLovedOnesKey = Constant.USER_KEY_PREFIX + userId + ":loved_ones";
        if (template.opsForSet().remove(userLovedOnesKey, lovedOneId) > 0) {
            return true;
        }
        return false;

    }

    public void deleteLovedOneData(String userId) {
        String lovedOneKey = Constant.LOVED_ONE_KEY_PREFIX + userId;
        template.delete(lovedOneKey);
    }

    public void deleteLovedOneInviteToken(String lovedOneId) {
        String inviteTokenKey = Constant.LOVED_ONE_KEY_PREFIX + lovedOneId + ":invite_token";
        template.delete(inviteTokenKey);
    }

    public Set<Object> getAllLovedOnes(String userId) {
        String userLovedOnesKey = Constant.USER_KEY_PREFIX + userId + ":loved_ones";
        Set<Object> lovedOnes = template.opsForSet().members(userLovedOnesKey);
        return (lovedOnes != null) ? lovedOnes : Collections.emptySet();
    }

    public boolean isLovedOneLinked(String userId, String lovedOneId) {
        String userLovedOnesKey = Constant.USER_KEY_PREFIX + userId + ":loved_ones";
        return template.opsForSet().isMember(userLovedOnesKey, lovedOneId);
    }

    public Object hasLovedOneLinkedTelegramChatId(String lovedOneId) {
        String lovedOneKey = Constant.LOVED_ONE_KEY_PREFIX + lovedOneId;
        return template.opsForHash().get(lovedOneKey, Constant.LOVED_ONE_CHAT_ID_FIELD); 
    }

    public Map<Object,Object> getLovedOneData(String lovedOneId) {
        String lovedOneKey = Constant.LOVED_ONE_KEY_PREFIX + lovedOneId;
        Map<Object, Object> lovedOneHash = template.opsForHash().entries(lovedOneKey);
        return lovedOneHash;
    }

    public Object getLovedOneIdInviteToken(String lovedOneId) {
        String inviteTokenKey = Constant.LOVED_ONE_KEY_PREFIX + lovedOneId + ":invite_token";
        return template.opsForValue().get(inviteTokenKey);
    }

    public Map<Object, Object> getInviteData(String inviteToken) {
        String inviteKey = Constant.INVITE_KEY_PREFIX + inviteToken;
        Map<Object, Object> inviteDataHash = template.opsForHash().entries(inviteKey);
        return inviteDataHash;
    }

    
    
}
