package com.onlinebookstore.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.onlinebookstore.service.StockNotificationService;

@Controller
@RequestMapping("/notify")
public class StockNotificationController {

    private final StockNotificationService stockNotificationService;

    public StockNotificationController(StockNotificationService stockNotificationService) {
        this.stockNotificationService = stockNotificationService;
    }

    @PostMapping("/{barcode}")
    public String register(@PathVariable String barcode, Principal principal,
            RedirectAttributes redirectAttributes) {
        boolean added = stockNotificationService.register(barcode, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage",
                added ? "We'll email you when this book is back in stock."
                        : "You're already on the notification list for this book.");
        return "redirect:/books/" + barcode;
    }
}
