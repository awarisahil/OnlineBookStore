package com.onlinebookstore.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.onlinebookstore.service.ReviewService;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public String add(@RequestParam String bookId, @RequestParam int rating, @RequestParam String text,
            Principal principal, RedirectAttributes redirectAttributes) {
        try {
            reviewService.addOrUpdate(bookId, principal.getName(), rating, text);
            redirectAttributes.addFlashAttribute("successMessage", "Thanks — your review is published.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/books/" + bookId;
    }
}
