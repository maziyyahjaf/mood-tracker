package com.example.maziyyah.mood_tracker.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.maziyyah.mood_tracker.service.InviteService;

@Controller
@RequestMapping("/invite")
public class InviteController {

    @Value("${telegram.bot.base.url}")
    private String telegramBotBaseUrl;

    private final InviteService inviteService;

    public InviteController(InviteService inviteService) {
        this.inviteService = inviteService;
    }


    @GetMapping("")
    public String showInviteInstructions(@RequestParam("token") String inviteToken, Model model) {

        String name = inviteService.getInviterUserDetails(inviteToken);

        // generate the link to Telegram with the inviteToken code pre-filled
        // String telegramBotUrl = telegramBotBaseUrl + "lovedone%3A" + inviteToken;
        String telegramBotUrl = telegramBotBaseUrl + inviteToken;

        System.out.println(telegramBotUrl);
        model.addAttribute("telegramBotUrl", telegramBotUrl);
        model.addAttribute("invitedBy", name);
        
        return "inviteInstructions";
    }

}
