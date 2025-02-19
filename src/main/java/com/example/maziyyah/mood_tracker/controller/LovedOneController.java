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

        // add manual validations?
        if (!validateLovedOneInputs(name, contact, relationship, model)) {
            return "addLovedOne";
        }
        // get user id from session
        String userId = (String) session.getAttribute("userId");
        LovedOne lovedOne = new LovedOne(name, contact, relationship);
        lovedOneService.addLovedOne(userId, lovedOne);
        String inviteLink = lovedOneService.generateInviteLink(userId, lovedOne);

        model.addAttribute("inviteLink", inviteLink);
        return "successAddLovedOne3";

    }

    @GetMapping("/{lovedOneId}/delete")
    public String removeLovedOne(@PathVariable("lovedOneId") String lovedOneId, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        logger.info("lovedOneId from delete: {} ", lovedOneId);
        lovedOneService.deleteLovedOneData(userId, lovedOneId);
        return "redirect:/lovedones";
    }

    @GetMapping("/{lovedOneId}/resend")
    public String resendInvite(@PathVariable("lovedOneId") String lovedOneId, HttpSession session, Model model) {
        // redirect to invite success??
        // get user id from session
        logger.info("lovedOneId from resend: {} ", lovedOneId);
        String userId = (String) session.getAttribute("userId");
        String inviteToken = lovedOneService.resendInvite(userId, lovedOneId);
        String inviteLink = lovedOneService.generateNewInviteLink(inviteToken);
        model.addAttribute("inviteLink", inviteLink);

        return "successAddLovedOne3";
    }

    @GetMapping("/invite/success")
    public String showSuccessInvitePage(@RequestParam("link") String inviteLink, Model model) {
        // shareable link
        model.addAttribute("inviteLink", inviteLink);
        return "successAddLovedOne3";
    }

    public boolean validateLovedOneInputs(String name, String contact, String relationship, Model model) {
        String nameError = null;
        String contactError = null;
        String relationshipError = null;

        // Trim inputs
        name = name.trim();
        contact = contact.trim();
        relationship = relationship.trim();

        // Validate name
        if (name.isEmpty() || name.length() < 2) {
            nameError = "Please enter a name with at least 2 characters to continue.";
        } else if (!name.matches("^[a-zA-Z]+[a-zA-Z .'-]{0,48}[a-zA-Z]$")) {
            nameError = "Their name should be 2-50 characters and can include letters, spaces, dots, hyphens, or apostrophes. Let's keep it simple and heartfelt!";
        }

        // Validate contact
        if (contact.isEmpty()) {
            contactError = "It looks like the contact number is missing.";
        } else if (!contact.matches("^\\+?[0-9]{7,15}$")) {
            contactError = "Their contact should be a valid number with 7 to 15 digits. You can include a '+' if it's an international number. Easy and clear!";
        }

        // Validate relationship
        if (relationship.isEmpty()) {
            relationshipError = "We’d love to know your relationship with them. Could you fill this in?";
        } else if (!relationship.matches("^[a-zA-Z][a-zA-Z\\s.,!?'-]{0,48}[a-zA-Z]$")) {
            relationshipError = "Describe your connection in 1-50 characters using letters, spaces, and common punctuation. Let's keep it meaningful and clear!";
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
