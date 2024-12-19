package com.example.maziyyah.mood_tracker.repository;

import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.example.maziyyah.mood_tracker.constant.Constant;
import com.example.maziyyah.mood_tracker.util.Utils;

@Repository
public class TelegramNotificationRepo {

    @Qualifier(Utils.template01)
    private final RedisTemplate<String, Object> template;

    public TelegramNotificationRepo(@Qualifier(Utils.template01) RedisTemplate<String, Object> template) {
        this.template = template;
    }

    public Object getUserChatId(String userId) {
        return template.opsForHash().get(Constant.userChatIds, userId);
    }

    public Set<Object> getAllLovedOnes(String userId) {
        String userLovedOnesKey = Constant.USER_KEY_PREFIX + userId + ":loved_ones";
        return template.opsForSet().members(userLovedOnesKey);

    }

    public Object getLovedOneChatId(String lovedOneId) {
        String lovedOneKey = Constant.LOVED_ONE_KEY_PREFIX + lovedOneId;
        return template.opsForHash().get(lovedOneKey, Constant.LOVED_ONE_CHAT_ID_FIELD);

    }
    
}
