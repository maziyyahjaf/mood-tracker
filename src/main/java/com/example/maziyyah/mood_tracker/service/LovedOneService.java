package com.example.maziyyah.mood_tracker.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.maziyyah.mood_tracker.model.LovedOne;
import com.example.maziyyah.mood_tracker.repository.LovedOneRepository;

@Service
public class LovedOneService {

    @Value("${app.invite.url}")
    private String appInviteUrl;
    private final LovedOneRepository lovedOneRepository;

    public LovedOneService(LovedOneRepository lovedOneRepository) {
        this.lovedOneRepository = lovedOneRepository;
    }

    public String generateInviteLink(String userId, LovedOne lovedOne) {
        String inviteToken = generateInviteToken();
        lovedOneRepository.generateInviteLink(userId, lovedOne, inviteToken);
        String inviteLink = appInviteUrl + inviteToken;
    
        return inviteLink;

    }

    public String generateInviteToken(){
        String inviteToken = UUID.randomUUID().toString().substring(0,8);
        return inviteToken;
    }

    public void addLovedOne(String userId, LovedOne lovedOne) {
        lovedOneRepository.addLovedOne(userId, lovedOne);
    }


    
    
}
