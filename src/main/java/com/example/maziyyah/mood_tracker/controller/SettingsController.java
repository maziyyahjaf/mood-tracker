package com.example.maziyyah.mood_tracker.controller;

// import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.maziyyah.mood_tracker.model.ProfileUpdateDTO;
import com.example.maziyyah.mood_tracker.model.TimeZoneOption;
import com.example.maziyyah.mood_tracker.model.User;
import com.example.maziyyah.mood_tracker.service.UserService;
import com.example.maziyyah.mood_tracker.util.TimeZoneUtils;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/profile")
public class SettingsController {

    @Value("${telegram.bot.base.url}")
    private String telegramBotBaseUrl;

    private final UserService userService;

    public SettingsController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/settings")
    public String showProfileSettings(HttpSession session, Model model) {
        // Retrieve userId from session
        String userId = (String) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login"; // Redirect to login if not authenticated
        }

        // check if user has telegram linked
        if (!userService.checkIfUserHasLinkedTelegramAccount(userId)) {
            // get linking code
            String linkingCode = userService.getUserLinkingCode(userId);
            // generate the link to Telegram with the linking code pre-filled
            String telegramBotUrl = telegramBotBaseUrl + linkingCode;
            model.addAttribute("telegramBotUrl", telegramBotUrl);
        }

        // Fetch user details from Redis
        User user = userService.getUserDetailsByUserId(userId);
        ProfileUpdateDTO dto = new ProfileUpdateDTO();
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setAlertThreshold(user.getAlertThreshold());
        dto.setEncouragementOptIn(user.isEncouragementOptIn());
        dto.setTimeZone(user.getTimeZone());

        // Fetch the time zone options using the utility method
        List<TimeZoneOption> timeZones = TimeZoneUtils.getTimeZoneOptions();
        model.addAttribute("timeZones", timeZones); // Pass time zone options to the view

        model.addAttribute("profileUpdateDTO", dto);
        model.addAttribute("originalUsername", user.getUsername());
        // model.addAttribute("timeZones", ZoneId.getAvailableZoneIds()); // Pass all
        // available time zones

        return "profileSettings1";
    }

    @PostMapping("/settings")
    public String updateProfile(@Valid @ModelAttribute("profileUpdateDTO") ProfileUpdateDTO entity,
            BindingResult bindingResult, @RequestParam("originalUsername") String originalUsername,
            Model model,
            HttpSession session) {

        // System.out.println("Original Username: " + originalUsername);
        // System.out.println("Updated bad day alert threshold: " +
        // entity.getAlertThreshold());
        String userId = (String) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            // model.addAttribute("timeZones", ZoneId.getAvailableZoneIds()); // Pass all available time zones

            // Fetch the time zone options using the utility method
            List<TimeZoneOption> timeZones = TimeZoneUtils.getTimeZoneOptions();
            model.addAttribute("timeZones", timeZones); // Pass time zone options to the view

            model.addAttribute("originalUsername", originalUsername);
            return "profileSettings1"; // Return with validation errors
        }

        // Check if the username was changed
        if (!entity.getUsername().equals(originalUsername)) {
            // perform "username already exists" check
            if (!userService.updateUsername(originalUsername, entity.getUsername())) {
                bindingResult.rejectValue("username", "error.username", "This username is already taken.");

                // Fetch the time zone options using the utility method
                List<TimeZoneOption> timeZones = TimeZoneUtils.getTimeZoneOptions();
                model.addAttribute("timeZones", timeZones); // Pass time zone options to the view

                // model.addAttribute("timeZones", ZoneId.getAvailableZoneIds()); // Pass all
                // available time zones
                model.addAttribute("originalUsername", originalUsername);
                return "profileSettings1";
            }
        }

        // Fetch current user details from Redis
        User user = userService.getUserDetailsByUserId(userId);

        // Update password
        if (entity.getPassword() != null && !entity.getPassword().isEmpty()) {
            userService.updatePassword(userId, entity.getPassword());
        }

        // Update encouragementOptIn
        // set the value of encourageOptIn
        // set the value of alert threshold
        // set the value of name?
        user.setName(entity.getName());
        user.setAlertThreshold(entity.getAlertThreshold());
        user.setEncouragementOptIn(entity.isEncouragementOptIn());
        user.setTimeZone(entity.getTimeZone());

        userService.updateUserDetails(userId, user);

        return "redirect:/profile/success"; // redirect on successful update

    }

    @GetMapping("/success")
    public String updateUserDetailsSuccess() {
        return "successUpdateDetails";
    }
}
