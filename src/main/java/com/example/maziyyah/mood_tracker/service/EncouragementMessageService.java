package com.example.maziyyah.mood_tracker.service;

import org.springframework.stereotype.Service;

import com.example.maziyyah.mood_tracker.repository.EncouragementMessageRepo;

@Service
public class EncouragementMessageService {
    
    // this will generate the prompt to send to external LLM
    private final EncouragementMessageRepo encouragementMessageRepo;

    public EncouragementMessageService(EncouragementMessageRepo encouragementMessageRepo) {
        this.encouragementMessageRepo = encouragementMessageRepo;
    }
    
    // i need the long epochDay so i can fetch the mood entries logged for the day
    // i need to format the entries to build the prompt to send to Gemini
    // then i need to save the message received from Gemini into redis with the proper key

    // have a generatePrompt method
    
}
