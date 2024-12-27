package com.example.maziyyah.mood_tracker.component;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.maziyyah.mood_tracker.constant.Constant;
import com.example.maziyyah.mood_tracker.service.EncouragementMessageService;

@Component
public class JobProcesser {

    private final RedisJobQueue redisJobQueue;
    private final EncouragementMessageService encouragementMessageService;
    private final DailyRequestTracker requestTracker;

    public JobProcesser(RedisJobQueue redisJobQueue, EncouragementMessageService encouragementMessageService,
            DailyRequestTracker requestTracker) {
        this.redisJobQueue = redisJobQueue;
        this.encouragementMessageService = encouragementMessageService;
        this.requestTracker = requestTracker;
    }

    @Scheduled(fixedRate = 30000) // 30 seconds interval
    public void processQueue() {
        if (!redisJobQueue.isEmpty() && requestTracker.getRequestCount() < Constant.LLM_REQUEST_DAILY_LIMIT) {
            String userId = redisJobQueue.dequeue();
            if (userId != null) {
                encouragementMessageService.generateAndSaveMessage(userId);
                requestTracker.increment();
            }
        }
    }
    
}
