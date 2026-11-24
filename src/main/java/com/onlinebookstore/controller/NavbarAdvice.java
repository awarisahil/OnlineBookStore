package com.onlinebookstore.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.onlinebookstore.model.Cart;
import com.onlinebookstore.service.CartService;
import com.onlinebookstore.service.ContactService;
import com.onlinebookstore.service.WishlistService;

/** Supplies the counts shown in the navbar on every page. */
@ControllerAdvice
public class NavbarAdvice {

    private final CartService cartService;
    private final WishlistService wishlistService;
    private final ContactService contactService;

    public NavbarAdvice(CartService cartService, WishlistService wishlistService, ContactService contactService) {
        this.cartService = cartService;
        this.wishlistService = wishlistService;
        this.contactService = contactService;
    }

    @ModelAttribute
    public void navbarCounts(HttpSession session, Authentication authentication, Model model) {
        try {
            Cart cart = cartService.getCart(session);
            int cartCount = cart.getLines().values().stream().mapToInt(Integer::intValue).sum();
            model.addAttribute("cartCount", cartCount);
        } catch (RuntimeException ex) {
            model.addAttribute("cartCount", 0);
        }

        if (authentication != null && authentication.isAuthenticated()) {
            boolean seller = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SELLER"));
            String name = authentication.getName();
            try {
                model.addAttribute("wishlistCount",
                        seller ? 0 : wishlistService.count(name));
                model.addAttribute("unreadMessages",
                        seller ? contactService.unreadCount() : 0);
            } catch (RuntimeException ex) {
                model.addAttribute("wishlistCount", 0);
                model.addAttribute("unreadMessages", 0);
            }
        } else {
            model.addAttribute("wishlistCount", 0);
            model.addAttribute("unreadMessages", 0);
        }
    }
}
