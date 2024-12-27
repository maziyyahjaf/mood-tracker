package com.example.maziyyah.mood_tracker.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.maziyyah.mood_tracker.constant.Constant;
import com.example.maziyyah.mood_tracker.service.EncouragementMessageService;
import com.example.maziyyah.mood_tracker.util.Utils;

@Component
public class JobProcesser {

    private final RedisJobQueue redisJobQueue;
    private final EncouragementMessageService encouragementMessageService;
    private final DailyRequestTracker requestTracker;

    private static final Logger logger = LoggerFactory.getLogger(JobProcesser.class);

    @Qualifier(Utils.template02)
    private final RedisTemplate<String, String> template;

    public JobProcesser(RedisJobQueue redisJobQueue, EncouragementMessageService encouragementMessageService,
            DailyRequestTracker requestTracker, @Qualifier(Utils.template02) RedisTemplate<String, String> template) {
        this.redisJobQueue = redisJobQueue;
        this.encouragementMessageService = encouragementMessageService;
        this.requestTracker = requestTracker;
        this.template = template;
    }

    @Scheduled(fixedDelay = 30000) // 30 seconds interval
    public void processQueue() {

        if (redisJobQueue.isEmpty()) {
            logger.info("Redis job queue is empty. Skipping job processing.");
            return;
        }
        if (requestTracker.getRequestCount() >= Constant.LLM_REQUEST_DAILY_LIMIT) {
            logger.info("Daily limit reached. Skipping job processing.");
            return;
        }

        String userId = redisJobQueue.dequeue();
        if (userId != null) {
            encouragementMessageService.generateAndSaveMessage(userId);
            requestTracker.increment();
            // Enqueue to notificationQueue for NotificationSender
            template.opsForList().rightPush("notificationQueue", userId);
        }

    }

}
