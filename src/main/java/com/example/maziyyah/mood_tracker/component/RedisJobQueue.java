package com.example.maziyyah.mood_tracker.component;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.maziyyah.mood_tracker.constant.Constant;
import com.example.maziyyah.mood_tracker.util.Utils;

@Component
public class RedisJobQueue {
    
    @Qualifier(Utils.template02)
    private final RedisTemplate<String, String> template;

    public RedisJobQueue(RedisTemplate<String, String> template) {
        this.template = template;
    }

    // add user ID to the queue
    public void enqueue(String userId) {
        template.opsForList().rightPush(Constant.QUEUE_KEY, userId);
    }

    // Retrieve and remove the next user ID from the queue
    public String dequeue() {
        String userId = template.opsForList().leftPop(Constant.QUEUE_KEY);
        return userId;
    }

    // check if the queue is empty
    public boolean isEmpty() {
        return template.opsForList().size(Constant.QUEUE_KEY) == 0;
    }

    // get the size of the queue
    public long size() {
        return template.opsForList().size(Constant.QUEUE_KEY);
    }
}
