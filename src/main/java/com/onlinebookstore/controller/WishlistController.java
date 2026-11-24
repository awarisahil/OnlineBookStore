package com.onlinebookstore.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.onlinebookstore.service.WishlistService;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public String view(Principal principal, Model model) {
        model.addAttribute("items", wishlistService.forCustomer(principal.getName()));
        return "wishlist";
    }

    @PostMapping("/toggle/{barcode}")
    public String toggle(@PathVariable String barcode, Principal principal,
            RedirectAttributes redirectAttributes) {
        boolean added = wishlistService.toggle(principal.getName(), barcode);
        redirectAttributes.addFlashAttribute("successMessage",
                added ? "Added to your wishlist." : "Removed from your wishlist.");
        return "redirect:/books/" + barcode;
    }

    @PostMapping("/remove/{barcode}")
    public String remove(@PathVariable String barcode, Principal principal) {
        wishlistService.remove(principal.getName(), barcode);
        return "redirect:/wishlist";
    }
}
