package com.onlinebookstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.onlinebookstore.service.AuditService;
import com.onlinebookstore.service.ReviewService;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;
    private final AuditService auditService;

    public AdminReviewController(ReviewService reviewService, AuditService auditService) {
        this.reviewService = reviewService;
        this.auditService = auditService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("reviews", reviewService.all());
        return "admin/reviews";
    }

    @PostMapping("/{id}/hide")
    public String hide(@PathVariable Long id) {
        reviewService.setHidden(id, true);
        auditService.record("admin", "REVIEW_HIDE", "review " + id);
        return "redirect:/admin/reviews";
    }

    @PostMapping("/{id}/show")
    public String show(@PathVariable Long id) {
        reviewService.setHidden(id, false);
        auditService.record("admin", "REVIEW_SHOW", "review " + id);
        return "redirect:/admin/reviews";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reviewService.delete(id);
        auditService.record("admin", "REVIEW_DELETE", "review " + id);
        redirectAttributes.addFlashAttribute("successMessage", "Review deleted.");
        return "redirect:/admin/reviews";
    }
}
