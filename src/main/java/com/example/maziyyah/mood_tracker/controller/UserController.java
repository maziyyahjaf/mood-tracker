package com.example.maziyyah.mood_tracker.controller;

import java.time.ZoneId;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.maziyyah.mood_tracker.model.User;
import com.example.maziyyah.mood_tracker.model.UserDTO;
import com.example.maziyyah.mood_tracker.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping
public class UserController {

    @Value("${telegram.bot.base.url}")
    private String telegramBotBaseUrl;

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = { "/", "login" })
    public String showLoginPage(@RequestParam(name = "logout", required = false) String logout, Model model) {
        Random random = new Random();
        String logoutMessage;

        if ("true".equals(logout)) {
            String[] message = {
                "You’re logged out! Take care and check back soon to log how you’re feeling—we’re here for you.",
                "All set! Don’t forget to come back and share how your day is going. Your next mood check is waiting.",
                "Logged out and ready to go! Remember, logging your mood is just a click away—we can’t wait to hear from you.",
                "Signed out successfully! Keep checking in with us—your next mood log matters.",
                "Goodbye for now! Don’t forget to swing by again soon to track how you’re feeling.",
                "You’re signed out! Your moods matter, and we can’t wait to hear from you again.",
                "Logged out, but don’t stay away too long! Your next entry is waiting whenever you are.",
                "You’re all set! Stay mindful, and check back soon to keep tracking your mood journey."
            };
            logoutMessage = message[random.nextInt(message.length)];
            model.addAttribute("logoutMessage", logoutMessage);

        }        
        return "userLogin3";
    }

    @PostMapping("/login")
    public String processUserCredentials(@RequestParam("username") String username,
            @RequestParam("password") String password, Model model, HttpServletRequest request) {

        if (!userService.login(username, password)) {
            // need to check if username exists
            // if username exists, then check password
            // need to add error attribute in page
            // should differentiate if username or password is incorrect?
            model.addAttribute("error", "Username and/or password is incorrect.");
            return "userLogin3";
        }

        // Invalidate old session and creat a new one to avoid session fixation attacks
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        session = request.getSession(true); // create a new session

        Optional<User> userOptional = userService.getUserByUsername(username);
        String userId = userOptional.get().getUserId();
        session.setAttribute("userId", userId);
        return "redirect:/moods"; // in the mood controller
    }

    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        // add validations to the UserDTO model
        UserDTO userDTO = new UserDTO();
        model.addAttribute("userDTO", userDTO);
        model.addAttribute("timeZones", ZoneId.getAvailableZoneIds()); // Pass all available time zones
        return "userRegistration";
    }

    @PostMapping("/register")
    public String processUserRegistration(@Valid @ModelAttribute("userDTO") UserDTO entity,
            BindingResult results, Model model) {

        if (results.hasErrors()) {
            model.addAttribute("timeZones", ZoneId.getAvailableZoneIds()); // Pass all available time zones
            return "userRegistration";
        }

        // need to check if username is available etc
        User user = new User(entity.getUsername(), entity.getName(), entity.getPassword(), entity.getTimeZone());
        String linkingCode = userService.generateLinkingCode();

        if (!userService.addUser(user, linkingCode)) {
            model.addAttribute("error", "Username is taken.");
            return "userRegistration";
        }

        // generate the link to Telegram with the linking code pre-filled
        String telegramBotUrl = telegramBotBaseUrl + linkingCode;
        model.addAttribute("telegramBotUrl", telegramBotUrl);

        return "successRegistration3";

    }

}
