package com.example.maziyyah.mood_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MoodTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoodTrackerApplication.class, args);
	}

}
