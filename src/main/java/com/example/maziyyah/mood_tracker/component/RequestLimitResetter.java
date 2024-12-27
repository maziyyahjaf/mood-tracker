package com.example.maziyyah.mood_tracker.component;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RequestLimitResetter {
    
    private final DailyRequestTracker requestTracker;

    public RequestLimitResetter(DailyRequestTracker requestTracker) {
        this.requestTracker = requestTracker;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Midnight reset -> utc??
    public void resetDailyLimit() {
        requestTracker.reset();
        System.out.println("Request limit reset at midnight.");

    }
}
