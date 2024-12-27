package com.example.maziyyah.mood_tracker.component;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.maziyyah.mood_tracker.model.User;
import com.example.maziyyah.mood_tracker.service.EncouragementMessageService;
import com.example.maziyyah.mood_tracker.service.TelegramNotificationService;
import com.example.maziyyah.mood_tracker.service.UserService;

@Component
public class NotificationSender {
    
    private final UserService userService;
    private final TelegramNotificationService telegramNotificationService;
    private final EncouragementMessageService encouragementMessageService;

    private static final Logger logger = LoggerFactory.getLogger(NotificationSender.class);


    public NotificationSender(UserService userService, TelegramNotificationService telegramNotificationService,
            EncouragementMessageService encouragementMessageService) {
        this.userService = userService;
        this.telegramNotificationService = telegramNotificationService;
        this.encouragementMessageService = encouragementMessageService;
    }

    @Scheduled(cron = "0 * * * * ?") // Runs every minute
    public void sendScheduledNotifications() {
        List<User> usersWithNotificationsDue = userService.getUsersWithNotificationsDue();
        
        if (usersWithNotificationsDue.isEmpty()) {
            logger.info("No users with notifications due this hour.");
            return;
        }

        for (User user : usersWithNotificationsDue) {
            String encouragementMessage = encouragementMessageService.getMessageFromRedis(user.getUserId());
            if (encouragementMessage != null) {
                telegramNotificationService.sendScheduledEncouragementMessage(user.getUserId(), encouragementMessage);
            } else {
                logger.warn("No precomputed message found for user {}.", user.getUserId());
            }
        }


    }

    
}
