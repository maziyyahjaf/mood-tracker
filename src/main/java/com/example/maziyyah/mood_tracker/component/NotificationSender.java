package com.example.maziyyah.mood_tracker.component;

// import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// import com.example.maziyyah.mood_tracker.model.User;
import com.example.maziyyah.mood_tracker.service.EncouragementMessageService;
import com.example.maziyyah.mood_tracker.service.TelegramNotificationService;
// import com.example.maziyyah.mood_tracker.service.UserService;
import com.example.maziyyah.mood_tracker.util.Utils;

@Component
public class NotificationSender {

    // private final UserService userService;
    private final TelegramNotificationService telegramNotificationService;
    private final EncouragementMessageService encouragementMessageService;

    @Qualifier(Utils.template02)
    private final RedisTemplate<String, String> template;

    private static final Logger logger = LoggerFactory.getLogger(NotificationSender.class);

    public NotificationSender(TelegramNotificationService telegramNotificationService,
            EncouragementMessageService encouragementMessageService,
            @Qualifier(Utils.template02) RedisTemplate<String, String> template) {
        this.telegramNotificationService = telegramNotificationService;
        this.encouragementMessageService = encouragementMessageService;
        this.template = template;
    }

    @Scheduled(cron = "0 * * * * ?") // Runs every minute
    public void sendScheduledNotifications() {
        // Dequeue from Redis List
        String userId = template.opsForList().leftPop("notificationQueue");

        // Process until the queue is empty
        while (userId != null) {
            // Fetch the precomputed encouragement message
            String encouragementMessage = encouragementMessageService.getMessageFromRedis(userId);

            // Send the message if available
            if (encouragementMessage != null) {
                telegramNotificationService.sendScheduledEncouragementMessage(userId, encouragementMessage);
            } else {
                logger.warn("No precomputed message found for user {}.", userId);
            }

            // Fetch the next userId in the queue
            userId = template.opsForList().leftPop("notificationQueue");
        }

        logger.info("Finished processing notification queue.");
        // List<User> usersWithNotificationsDue =
        // userService.getUsersWithNotificationsDue();

        // if (usersWithNotificationsDue.isEmpty()) {
        // logger.info("No users with notifications due this hour.");
        // return;
        // }

        // for (User user : usersWithNotificationsDue) {
        // String encouragementMessage =
        // encouragementMessageService.getMessageFromRedis(user.getUserId());
        // if (encouragementMessage != null) {
        // telegramNotificationService.sendScheduledEncouragementMessage(user.getUserId(),
        // encouragementMessage);
        // } else {
        // logger.warn("No precomputed message found for user {}.", user.getUserId());
        // }
        // }

    }

}
