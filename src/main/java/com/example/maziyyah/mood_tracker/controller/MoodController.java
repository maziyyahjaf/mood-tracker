package com.example.maziyyah.mood_tracker.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.maziyyah.mood_tracker.model.DailyMoodSummary;
import com.example.maziyyah.mood_tracker.model.MoodEmoji;
import com.example.maziyyah.mood_tracker.model.MoodEntryView;
import com.example.maziyyah.mood_tracker.service.MoodTrackerService;
import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/moods")
public class MoodController {

    private final MoodTrackerService moodTrackerService;

    public MoodController(MoodTrackerService moodTrackerService) {
        this.moodTrackerService = moodTrackerService;
    }

    @GetMapping("")
    public String showMoodDashboard() {
        return "moodDashboard";
    }

    @GetMapping("/log")
    public String showMoodLogPage() {
        return "moodEntry";
    }

    // handles form submission and redirects to daily view
    @PostMapping("/log")
    public String logMood(@RequestParam("moodScore") Integer moodScore,
            @RequestParam("note") String note,
            @RequestParam(value = "tags", required = false, defaultValue = "") String tagsString, HttpSession session)
            throws JsonProcessingException {

        // get userId from session
        String userId = (String) session.getAttribute("userId");

        // Fetch the user's time zone
        String userTimeZone = moodTrackerService.getUserTimeZone(userId);
        ZoneId userZone = ZoneId.of(userTimeZone);

        // get the current timestamp (when form is submitted)
        // get the current timestamp in UTC
        Instant now = Instant.now();
        // long timestampEpochMilli = now.toInstant(ZoneOffset.UTC).toEpochMilli();
        long timestampEpochMilli = now.toEpochMilli();

        // Convert timestamp to user's local time
        ZonedDateTime userTime = now.atZone(ZoneOffset.UTC).withZoneSameInstant(userZone);

        // extract the user's local date for grouping purposes
        // LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        LocalDate currentDate = userTime.toLocalDate();
        long epochDay = currentDate.toEpochDay(); // number of days since 1970-01-01 (local time)
        System.out.println("epochday" + epochDay);

        // hand null or empty tags
        // Convert tagsString to List<String>
        List<String> tags = tagsString.isEmpty() ? List.of() : Arrays.asList(tagsString.split(","));

        // save mood entry with user-local time
        moodTrackerService.addMoodEntry(userId, timestampEpochMilli, epochDay, moodScore, note, tags);

        // redirect to view all entries for that day
        return "redirect:/moods/dailyview/" + epochDay;
    }

    // handles daily view (show all the moods for the day)
    @GetMapping("/dailyview/{epochDay}")
    public String viewDailyLog(@PathVariable("epochDay") long epochDay, HttpSession session, Model model) {

        // get userId from session
        String userId = (String) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login"; // Redirect if not logged in
        }

        // get all the mood entries for the specified day
        List<MoodEntryView> moodEntries = moodTrackerService.getMoodEntryViewsForDay(userId, epochDay);
        moodEntries.sort((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()));

        for (MoodEntryView entry : moodEntries) {
            entry.setEmoji(MoodEmoji.getEmojiFor(entry.getMoodScore()));
        }

        // convert epochDay to LocalDate
        LocalDate date = LocalDate.ofEpochDay(epochDay);

        // add entries to the view model
        model.addAttribute("date", date);
        model.addAttribute("moodEntries", moodEntries);
        model.addAttribute("epochDay", epochDay);
        return "dailyView";
    }

    @GetMapping("/tileview/{epochDay}")
    public String viewDailyViewTile(@PathVariable("epochDay") long epochDay, Model model) {
        model.addAttribute("epochDay", epochDay);
        return "dailyViewTile";
    }

    @GetMapping("/weeklyview")
    public String viewWeeklySummary(HttpSession session, Model model) {
        // get user id from session
        String userId = (String) session.getAttribute("userId");

        // get the last 7 days' summaries
        List<DailyMoodSummary> weeklySummaries = moodTrackerService.getWeeklySummary(userId);

        for (DailyMoodSummary summary : weeklySummaries) {
            summary.setEmoji(MoodEmoji.getEmojiFor(summary.getAverageMoodScore()));
        }

        model.addAttribute("weeklySummaries", weeklySummaries);
        return "weeklyView";
    }

}
