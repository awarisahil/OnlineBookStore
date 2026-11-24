package com.onlinebookstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.onlinebookstore.service.AuditService;
import com.onlinebookstore.service.ContactService;

@Controller
@RequestMapping("/admin/messages")
public class AdminMessageController {

    private final ContactService contactService;
    private final AuditService auditService;

    public AdminMessageController(ContactService contactService, AuditService auditService) {
        this.contactService = contactService;
        this.auditService = auditService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("messages", contactService.all());
        return "admin/messages";
    }

    @PostMapping("/{id}/read")
    public String markRead(@PathVariable Long id) {
        contactService.markRead(id);
        return "redirect:/admin/messages";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        contactService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Message deleted.");
        return "redirect:/admin/messages";
    }

    @GetMapping("/audit")
    public String auditLog(Model model) {
        model.addAttribute("entries", auditService.recent());
        return "admin/audit";
    }
}
