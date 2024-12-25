package com.example.maziyyah.mood_tracker.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.maziyyah.mood_tracker.model.LovedOne;
import com.example.maziyyah.mood_tracker.model.LovedOneDTO;
import com.example.maziyyah.mood_tracker.service.LovedOneService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/lovedones")
public class LovedOneController {

    private final LovedOneService lovedOneService;

    public LovedOneController(LovedOneService lovedOneService) {
        this.lovedOneService = lovedOneService;
    }

    @GetMapping("")
    public String showListOfLovedOnesForUser(HttpSession session, Model model) {
        // get user id from session
        String userId = (String) session.getAttribute("userId");
        Optional<List<String>> addedLovedOnesId = lovedOneService.getAllAddedLovedOnesId(userId);
        List<LovedOneDTO> addedLovedOnes = lovedOneService.getAllLovedOnes(addedLovedOnesId);
        model.addAttribute("addedLovedOnes", addedLovedOnes);
        
        return "lovedOnesList";
    }

    @GetMapping("/add")
    public String showAddLovedOnePage() {
        return "addLovedOne";
    }

    @PostMapping("/invite")
    public String createInviteLink(@RequestParam("name") String name,
            @RequestParam("contact") String contact,
            @RequestParam("relationship") String relationship,
            HttpSession session, Model model) {
        
        // get user id from session
        String userId = (String) session.getAttribute("userId");
        LovedOne lovedOne = new LovedOne(name, contact, relationship);
        lovedOneService.addLovedOne(userId, lovedOne);
        String inviteLink = lovedOneService.generateInviteLink(userId, lovedOne);

        // Redirect to a page where the user sees the invite link
        // return "redirect:/lovedones/invite/success?link=" + inviteLink;
        model.addAttribute("inviteLink", inviteLink);
        return "successAddLovedOne";

    }

    @GetMapping("/lovedones/{lovedOneId}/delete")
    public String removeLovedOne(@PathVariable("lovedOneId") String lovedOneId,  HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        lovedOneService.deleteLovedOneData(userId, lovedOneId);
        return "redirect:/lovedones";
    }

    @GetMapping("/lovedones/{lovedOneId}/resend")
    public String resendInvite(@PathVariable("lovedOneId") String lovedOneId, HttpSession session) {
        // redirect to invite success??
        return null;
    }

    @GetMapping("/invite/success")
    public String showSuccessInvitePage(@RequestParam("link") String inviteLink, Model model) {
        // shareable link
        model.addAttribute("inviteLink", inviteLink);
        return "successAddLovedOne";
    }

}
