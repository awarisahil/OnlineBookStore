package com.onlinebookstore.controller;

import java.security.Principal;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.onlinebookstore.model.Book;
import com.onlinebookstore.model.Cart;
import com.onlinebookstore.model.ShopOrder;
import com.onlinebookstore.service.BookService;
import com.onlinebookstore.service.CartService;
import com.onlinebookstore.service.OrderService;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final BookService bookService;
    private final CartService cartService;

    public OrderController(OrderService orderService, BookService bookService, CartService cartService) {
        this.orderService = orderService;
        this.bookService = bookService;
        this.cartService = cartService;
    }

    @GetMapping
    public String list(Authentication authentication, Model model) {
        model.addAttribute("orders", orderService.findForCustomer(authentication.getName()));
        return "orders/index";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Authentication authentication, Model model) {
        model.addAttribute("order", orderService.findForCustomer(id, authentication.getName()));
        return "orders/detail";
    }

    @GetMapping("/{id}/invoice")
    public String invoice(@PathVariable Long id, Authentication authentication, Model model) {
        model.addAttribute("order", orderService.findForCustomer(id, authentication.getName()));
        return "orders/invoice";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, @RequestParam(required = false) String reason,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelByCustomer(id, authentication.getName(),
                    reason == null ? "Cancelled by customer" : reason);
            redirectAttributes.addFlashAttribute("successMessage", "Order cancelled — stock has been returned.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    /** Copy every item of a past order back into the cart, respecting current stock. */
    @PostMapping("/{id}/reorder")
    public String reorder(@PathVariable Long id, Authentication authentication, HttpSession session,
            RedirectAttributes redirectAttributes) {
        ShopOrder order = orderService.findForCustomer(id, authentication.getName());
        Cart cart = cartService.getCart(session);
        int added = 0;
        int skipped = 0;
        for (var item : order.getItems()) {
            try {
                Book book = bookService.findByBarcode(item.getBarcode());
                int room = Math.max(0, book.getQuantity() - cart.get(book.getBarcode()));
                int toAdd = Math.min(item.getQuantity(), room);
                for (int i = 0; i < toAdd; i++) {
                    cart.add(book.getBarcode());
                    added++;
                }
                if (toAdd < item.getQuantity()) {
                    skipped += item.getQuantity() - toAdd;
                }
            } catch (RuntimeException ex) {
                skipped += item.getQuantity();
            }
        }
        if (added > 0) {
            redirectAttributes.addFlashAttribute("successMessage",
                    skipped > 0
                            ? "Added " + added + " item(s) to your cart (" + skipped + " skipped — not enough stock)."
                            : "Added " + added + " item(s) to your cart.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Nothing could be added — the books are out of stock or no longer available.");
        }
        return "redirect:/cart";
    }
}
