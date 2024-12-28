package com.example.maziyyah.mood_tracker.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        private static final Logger logger = LoggerFactory.getLogger(LovedOneController.class);


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
        
        // add  manual validations?
        if (!validateLovedOneInputs(name, contact, relationship, model)) {
            return "addLovedOne";
        }
        // get user id from session
        String userId = (String) session.getAttribute("userId");
        LovedOne lovedOne = new LovedOne(name, contact, relationship);
        lovedOneService.addLovedOne(userId, lovedOne);
        String inviteLink = lovedOneService.generateInviteLink(userId, lovedOne);

        model.addAttribute("inviteLink", inviteLink);
        return "successAddLovedOne";

    }

    @GetMapping("/{lovedOneId}/delete")
    public String removeLovedOne(@PathVariable("lovedOneId") String lovedOneId,  HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        logger.info("lovedOneId from delete: {} " ,lovedOneId);
        lovedOneService.deleteLovedOneData(userId, lovedOneId);
        return "redirect:/lovedones";
    }

    @GetMapping("/{lovedOneId}/resend")
    public String resendInvite(@PathVariable("lovedOneId") String lovedOneId, HttpSession session, Model model) {
        // redirect to invite success??
        // get user id from session
        logger.info("lovedOneId from resend: {} " ,lovedOneId);
        String userId = (String) session.getAttribute("userId");
        String inviteToken = lovedOneService.resendInvite(userId, lovedOneId);
        String inviteLink = lovedOneService.generateNewInviteLink(inviteToken);
        model.addAttribute("inviteLink", inviteLink);

        return "successAddLovedOne";
    }

    @GetMapping("/invite/success")
    public String showSuccessInvitePage(@RequestParam("link") String inviteLink, Model model) {
        // shareable link
        model.addAttribute("inviteLink", inviteLink);
        return "successAddLovedOne";
    }

    public boolean validateLovedOneInputs(String name, String contact, String relationship, Model model) {
        String nameError = null;
        String contactError = null;
        String relationshipError = null;

        if (name.isEmpty() || name.length() < 2) {
            nameError = "Please enter a name with at least 2 characters to continue.";
        }

        if (contact.isEmpty()) {
            contactError = "It looks like the contact number is missing.";
        }

        if (relationship.isEmpty()) {
            relationshipError = "We’d love to know your relationship with them. Could you fill this in?";
        }

        if (nameError != null || contactError != null || relationshipError != null) {
            model.addAttribute("nameError", nameError);
            model.addAttribute("name", name);
            model.addAttribute("contactError", contactError);
            model.addAttribute("contact", contact);
            model.addAttribute("relationshipError", relationshipError);
            model.addAttribute("relationship", relationship);

            return false;
        }
        return true;
    }

}
