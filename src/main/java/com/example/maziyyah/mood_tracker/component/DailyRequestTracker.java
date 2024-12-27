package com.example.maziyyah.mood_tracker.component;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;


@Component
public class DailyRequestTracker {
    private final AtomicInteger requestCount = new AtomicInteger(0);

    // get the current count
    public int getRequestCount() {
        return requestCount.get();
    }

    // increment the counter
    public void increment() {
        requestCount.incrementAndGet();
    }

    // reset the counter
    public void reset() {
        requestCount.set(0);
    }

}
