package com.example.maziyyah.mood_tracker.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.maziyyah.mood_tracker.model.DailyMoodSummary;
import com.example.maziyyah.mood_tracker.model.MoodEntryView;
import com.example.maziyyah.mood_tracker.service.MoodTrackerService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/moods/api")
public class MoodApiController {

    private final MoodTrackerService moodTrackerService;

    public MoodApiController(MoodTrackerService moodTrackerService) {
        this.moodTrackerService = moodTrackerService;
    }

    // new API endpoint to get mood entries for a specific day as JSON
    @GetMapping("/dailyview/{epochDay}")
    public ResponseEntity<List<MoodEntryView>> getMoodEntriesForDay(@PathVariable("epochDay") long epochDay, HttpSession session) {
        // get userid from session
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(null); // User is not authenticated
        }

        // get mood entries for the day for the specific user
        List<MoodEntryView> moodEntries = moodTrackerService.getMoodEntryViewsForDay(userId, epochDay);

        // sort by timestamp (earliest to latest)
        moodEntries.sort(Comparator.comparing(MoodEntryView::getTimestamp));

        // If no mood entries are found, return 204 (No Content)
        if (moodEntries.isEmpty()) {
            return ResponseEntity.status(204).build();
        }

        return ResponseEntity.ok().body(moodEntries);
    }

    @GetMapping("/weeklyview")
    public ResponseEntity<List<DailyMoodSummary>> getWeeklySummary(HttpSession session) {
         // get userid from session
         String userId = (String) session.getAttribute("userId");
         if (userId == null) {
             return ResponseEntity.status(401).body(null); // User is not authenticated
         }

         List<DailyMoodSummary> weeklySummaries = moodTrackerService.getWeeklySummary(userId);

         if (weeklySummaries.isEmpty()) {
            return ResponseEntity.status(204).build();
         }

         return ResponseEntity.ok().body(weeklySummaries);
    }

    
    
    
}
