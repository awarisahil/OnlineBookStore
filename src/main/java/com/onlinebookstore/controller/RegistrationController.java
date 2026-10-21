package com.onlinebookstore.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.onlinebookstore.dto.RegistrationForm;
import com.onlinebookstore.exception.DuplicateEmailException;
import com.onlinebookstore.service.UserService;

@Controller
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String form(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegistrationForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            try {
                userService.register(form);
                return "redirect:/login?registered";
            } catch (DuplicateEmailException ex) {
                bindingResult.rejectValue("email", "duplicate", ex.getMessage());
            }
        }
        return "register";
    }
}
