package com.example.maziyyah.mood_tracker.repository;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.example.maziyyah.mood_tracker.constant.Constant;
import com.example.maziyyah.mood_tracker.util.Utils;

@Repository
public class WebhookRepository {

    @Qualifier(Utils.template01)
    private final RedisTemplate<String, Object> template;

    public WebhookRepository(@Qualifier(Utils.template01) RedisTemplate<String, Object> template) {
        this.template = template;
    }

    public Object checkIfLinkingCodeExist(String linkingCode) {
        return template.opsForHash().get(Constant.linkingCodes, linkingCode);
        
    }

    public void linkTelegramUserIdToAppUserId(String telegramId, String userId) {
        template.opsForHash().put(Constant.telegramUserIds, telegramId, userId);
    }

    public void linkUserIdToChatId(String userId, String chatId) {
        template.opsForHash().put(Constant.userChatIds, userId, chatId);
    }

    public Map<Object,Object> checkIfInviteTokenExists(String inviteToken) {
        String inviteKey = Constant.INVITE_KEY_PREFIX + inviteToken;
        Map<Object,Object> retrievedInviteData = template.opsForHash().entries(inviteKey);
        return retrievedInviteData;
        
    }

    public void linkLovedOneIdToChatId(String lovedOneId, String chatId) {
        String lovedOneKey = Constant.LOVED_ONE_KEY_PREFIX + lovedOneId;
        template.opsForHash().put(lovedOneKey, Constant.LOVED_ONE_CHAT_ID_FIELD, chatId);
    }

    public void deleteInviteToken(String inviteToken) {
        String inviteKey = Constant.INVITE_KEY_PREFIX + inviteToken;
        template.delete(inviteKey);
    }

    

    
}
