package com.onlinebookstore.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.onlinebookstore.dto.ContactForm;
import com.onlinebookstore.service.ContactService;

@Controller
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping("/contact")
    public String form(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ContactForm());
        }
        return "contact";
    }

    @PostMapping("/contact")
    public String submit(@Valid @ModelAttribute("form") ContactForm form, BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "contact";
        }
        contactService.submit(form.getName(), form.getEmail(), form.getSubject(), form.getMessage());
        model.addAttribute("sent", true);
        return "contact";
    }
}
