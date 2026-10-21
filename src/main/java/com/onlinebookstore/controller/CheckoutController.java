package com.onlinebookstore.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.onlinebookstore.dto.Receipt;
import com.onlinebookstore.dto.ReceiptLine;
import com.onlinebookstore.model.Cart;
import com.onlinebookstore.model.CartLine;
import com.onlinebookstore.service.BookService;
import com.onlinebookstore.service.CartService;

@Controller
public class CheckoutController {

    private final BookService bookService;
    private final CartService cartService;

    public CheckoutController(BookService bookService, CartService cartService) {
        this.bookService = bookService;
        this.cartService = cartService;
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
    public String pay(HttpSession session, RedirectAttributes redirectAttributes) {
        Cart cart = cartService.getCart(session);
        CartService.CartView view = cartService.getCartView(session);
        if (view.lines().isEmpty()) {
            return "redirect:/cart";
        }

        Map<String, Integer> items = new LinkedHashMap<>();
        view.lines().forEach(line -> items.put(line.getBarcode(), line.getQuantity()));
        bookService.purchase(items);

        Receipt receipt = new Receipt();
        receipt.setLines(view.lines().stream()
                .map(this::toReceiptLine)
                .toList());
        receipt.setTotal(view.total());
        receipt.setPlacedAt(LocalDateTime.now());

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

    private ReceiptLine toReceiptLine(CartLine line) {
        return new ReceiptLine(line.getBarcode(), line.getName(), null, line.getPrice(), line.getQuantity());
    }
}
