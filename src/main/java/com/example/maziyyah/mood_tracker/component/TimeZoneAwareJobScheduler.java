package com.example.maziyyah.mood_tracker.component;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

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
        Optional<List<String>> userRedisKeysOpt = userService.getAllUsersRedisKey();
        List<User> users = userService.getAllUsers(userRedisKeysOpt);
        if (users.isEmpty()) {
            logger.info("No registered users. Skipping job scheduling.");
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC")); // current time in UTC
        for (User user : users) {
            String userTimeZone = user.getTimeZone();
            try {
                ZoneId userZoneId = ZoneId.of(userTimeZone); // user's time zone
                // build the user's notification time in their time zone
                ZonedDateTime userNotificationTime = ZonedDateTime.now(userZoneId)
                        .withHour(user.getNotificationTime().getHour())
                        .withMinute(user.getNotificationTime().getMinute())
                        .withSecond(0)
                        .withNano(0);

                // Debug logs for clarity
                logger.info("Current time (UTC): {}", now);
                logger.info("User notification time (Local): {}", userNotificationTime);

                // compare using instant times
                if (userNotificationTime.toInstant().isBefore(now.toInstant()) && userNotificationTime.plusHours(1).toInstant().isAfter(now.toInstant())) {
                    redisJobQueue.enqueue(user.getUserId());
                    logger.info("Enqueued user {} for notification at {}", user.getUserId(), userNotificationTime);
                } else {
                    logger.info("Skipping user {} - Notification time {} not within the current hour", user.getUserId(),
                            userNotificationTime);
                }
            } catch (DateTimeException e) {
                logger.error("Invalid time zone for user {}: {}", user.getUserId(), userTimeZone, e);
            }

        }

    }

}
