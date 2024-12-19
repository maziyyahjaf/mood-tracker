package com.example.maziyyah.mood_tracker.service;

import org.springframework.stereotype.Service;

import com.example.maziyyah.mood_tracker.repository.InviteRepository;

@Service
public class InviteService {

    private final InviteRepository inviteRepository;

    public InviteService(InviteRepository inviteRepository) {
        this.inviteRepository = inviteRepository;
    }
    
    public String getInviterUserDetails(String inviteToken) {
        String name = inviteRepository.getInviterUserDetails(inviteToken);
        return name;
    }
}
