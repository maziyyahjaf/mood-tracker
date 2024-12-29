package com.example.maziyyah.mood_tracker.util;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
// import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.example.maziyyah.mood_tracker.model.TimeZoneOption;

public class TimeZoneUtils {

    public static List<TimeZoneOption> getTimeZoneOptions() {
        return ZoneId.getAvailableZoneIds().stream()
                .map(zoneId -> createTimeZoneOption(zoneId)) // Use helper method
                .filter(Objects::nonNull) // Filter out invalid zones
                .collect(Collectors.groupingBy(
                        timeZoneOption -> extractRegion(timeZoneOption.getZoneId()), // Extract region
                        TreeMap::new, // Maintain sorted regions
                        Collectors.minBy(Comparator.comparing(TimeZoneOption::getDisplayName)) // Pick smallest display
                                                                                               // name
                ))
                .values().stream()
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(TimeZoneOption::getDisplayName))
                .collect(Collectors.toList());
    }

    private static TimeZoneOption createTimeZoneOption(String zoneId) {
        try {
            ZoneId zone = ZoneId.of(zoneId);
            ZoneOffset offset = zone.getRules().getOffset(Instant.now());
            int offsetInSeconds = offset.getTotalSeconds();
            String offsetDisplay = String.format("UTC%s%02d:%02d",
                    offsetInSeconds == 0 ? "" : (offsetInSeconds > 0 ? "+" : "-"),
                    Math.abs(offsetInSeconds) / 3600,
                    Math.abs(offsetInSeconds) % 3600 / 60);
            String displayName = String.format("(%s) %s", offsetDisplay, zoneId);
            return new TimeZoneOption(zoneId, displayName);
        } catch (DateTimeException e) {
            return null;
        }
    }

    private static String extractRegion(String zoneId) {
        String[] parts = zoneId.split("/");
        return parts.length > 1 ? parts[0] : zoneId;
    }

}
