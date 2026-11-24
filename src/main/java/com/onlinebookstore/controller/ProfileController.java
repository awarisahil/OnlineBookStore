package com.onlinebookstore.controller;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.onlinebookstore.dto.ProfileForm;
import com.onlinebookstore.service.UserService;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String view(Authentication authentication, Model model) {
        if (!model.containsAttribute("form")) {
            var user = userService.findByEmail(authentication.getName());
            ProfileForm form = new ProfileForm();
            form.setFirstName(user.getFirstName());
            form.setLastName(user.getLastName());
            form.setAddress(user.getAddress());
            form.setPhone(user.getPhone());
            model.addAttribute("form", form);
        }
        return "profile";
    }

    @PostMapping
    public String update(@Valid @ModelAttribute("form") ProfileForm form, BindingResult bindingResult,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "profile";
        }
        userService.updateProfile(authentication.getName(), form.getFirstName(), form.getLastName(),
                form.getAddress(), form.getPhone());
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated.");
        return "redirect:/profile";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam String currentPassword, @RequestParam String newPassword,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        if (newPassword == null || newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "New password must be at least 6 characters.");
            return "redirect:/profile";
        }
        boolean changed = userService.changePassword(authentication.getName(), currentPassword, newPassword);
        if (changed) {
            redirectAttributes.addFlashAttribute("successMessage", "Password changed.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Your current password was not correct.");
        }
        return "redirect:/profile";
    }
}
