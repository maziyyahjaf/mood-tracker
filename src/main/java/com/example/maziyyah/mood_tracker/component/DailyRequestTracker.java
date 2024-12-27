package com.example.maziyyah.mood_tracker.component;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import com.example.maziyyah.mood_tracker.constant.Constant;

@Component
public class DailyRequestTracker {
    private final AtomicInteger requestCount = new AtomicInteger(0);

    // get the current count
    public int getRequestCount() {
        return requestCount.get();
    }

    // increment the counter
    public int increment() {
        return requestCount.incrementAndGet();
    }

    // reset the counter
    public void reset() {
        requestCount.set(0);
    }

    // check if the limit is reached
    public boolean isLimitReached() {
        return requestCount.get() >= Constant.LLM_REQUEST_DAILY_LIMIT;
    }
}
