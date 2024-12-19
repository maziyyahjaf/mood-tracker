package com.example.maziyyah.mood_tracker.repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

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
        String inviteKey = "invite:" + inviteToken;
        String lovedOneKey = "lovedOne:" + lovedOne.getLoveOneId();

        Map<String, String> lovedOneData = new HashMap<>();
        lovedOneData.put("name", lovedOne.getName());
        lovedOneData.put("contact", lovedOne.getContact());
        lovedOneData.put("relationship", lovedOne.getRelationship());

        Map<String, String> inviteData = new HashMap<>();
        inviteData.put("userId", userId);
        inviteData.put("lovedOneId", lovedOne.getLoveOneId());
        inviteData.put("expiresAt", String.valueOf(Instant.now().plusSeconds(604800).toEpochMilli())); // 7 day expiry

        template.opsForHash().putAll(lovedOneKey, lovedOneData);
        template.opsForHash().putAll(inviteKey, inviteData);
    }

    public void addLovedOne(String userId, LovedOne lovedOne) {
        String lovedOneId = lovedOne.getLoveOneId();
        String userLovedOnesKey = "user:" + userId + ":loved_ones";
        template.opsForSet().add(userLovedOnesKey, lovedOneId);
    }

    public void removeLovedOne(String userId, LovedOne lovedOne) {
        String lovedOneId = lovedOne.getLoveOneId();
        String userLovedOnesKey = "user:" + userId + ":loved_ones";
        template.opsForSet().remove(userLovedOnesKey, lovedOneId);

    }

    public Set<Object> getAllLovedOnes(String userId) {
        String userLovedOnesKey = "user:" + userId + ":loved_ones";
        return template.opsForSet().members(userLovedOnesKey);
    }

    public boolean isLovedOneLinked(String userId, String lovedOneId) {
        String userLovedOnesKey = "user:" + userId + ":loved_ones";
        return template.opsForSet().isMember(userLovedOnesKey, lovedOneId);
    }

    
    
}
