package com.example.maziyyah.mood_tracker.component;


import java.time.ZonedDateTime;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.maziyyah.mood_tracker.model.User;
import com.example.maziyyah.mood_tracker.service.UserService;

@Component
public class TimeZoneAwareJobScheduler {
    private final RedisJobQueue redisJobQueue;
    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(TimeZoneAwareJobScheduler.class);

    public TimeZoneAwareJobScheduler(RedisJobQueue redisJobQueue, UserService userService) {
        this.redisJobQueue = redisJobQueue;
        this.userService = userService;
    }

    @Scheduled(cron = "0 0 * * * ?") // Runs every hour
    public void scheduleJobs() {
        logger.info("Scheduled job triggered at {}", ZonedDateTime.now());
        
        List<User> usersWithNotificationsDue = userService.getUsersWithNotificationsDue();
        
        if (usersWithNotificationsDue.isEmpty()) {
            logger.info("No users with notifications due this hour.");
            return;
        }

        for (User user : usersWithNotificationsDue) {
            redisJobQueue.enqueue(user.getUserId());
            logger.info("Enqueued user {} for notification.", user.getUserId());
        }

    }

}
