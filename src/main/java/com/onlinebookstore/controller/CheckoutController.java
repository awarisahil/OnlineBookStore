package com.onlinebookstore.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.onlinebookstore.dto.Receipt;
import com.onlinebookstore.model.Cart;
import com.onlinebookstore.service.CartService;
import com.onlinebookstore.service.OrderService;

@Controller
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;

    public CheckoutController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        CartService.CartView view = cartService.getCartView(session);
        if (view.lines().isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("lines", view.lines());
        model.addAttribute("total", view.total());
        return "checkout";
    }

    @PostMapping("/pay")
    public String pay(Authentication authentication, HttpSession session, RedirectAttributes redirectAttributes) {
        Cart cart = cartService.getCart(session);
        CartService.CartView view = cartService.getCartView(session);
        if (view.lines().isEmpty()) {
            return "redirect:/cart";
        }

        Receipt receipt = orderService.placeOrder(authentication.getName(), view.lines());

        cart.clear();
        redirectAttributes.addFlashAttribute("receipt", receipt);
        return "redirect:/receipt";
    }

    @GetMapping("/receipt")
    public String receipt(@ModelAttribute("receipt") Receipt receipt) {
        if (receipt == null || receipt.getLines() == null || receipt.getLines().isEmpty()) {
            return "redirect:/books";
        }
        return "receipt";
    }
}
