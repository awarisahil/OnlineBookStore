package com.onlinebookstore.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.onlinebookstore.model.Book;
import com.onlinebookstore.model.Cart;
import com.onlinebookstore.service.BookService;
import com.onlinebookstore.service.CartService;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final BookService bookService;
    private final CartService cartService;

    public CartController(BookService bookService, CartService cartService) {
        this.bookService = bookService;
        this.cartService = cartService;
    }

    @PostMapping("/add/{barcode}")
    public String add(@PathVariable String barcode, HttpSession session, RedirectAttributes redirectAttributes) {
        Book book = bookService.findByBarcode(barcode);
        Cart cart = cartService.getCart(session);
        if (cart.get(barcode) >= book.getQuantity()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Only " + book.getQuantity() + " copies of \"" + book.getName() + "\" are available");
        } else {
            cart.add(barcode);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Added \"" + book.getName() + "\" to your cart");
        }
        return "redirect:/books";
    }

    @PostMapping("/inc/{barcode}")
    public String increment(@PathVariable String barcode, HttpSession session,
            RedirectAttributes redirectAttributes) {
        Book book = bookService.findByBarcode(barcode);
        Cart cart = cartService.getCart(session);
        if (cart.get(barcode) >= book.getQuantity()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Only " + book.getQuantity() + " copies of \"" + book.getName() + "\" are available");
        } else {
            cart.add(barcode);
        }
        return "redirect:/cart";
    }

    @PostMapping("/dec/{barcode}")
    public String decrement(@PathVariable String barcode, HttpSession session) {
        cartService.getCart(session).decrement(barcode);
        return "redirect:/cart";
    }

    @GetMapping
    public String view(HttpSession session, Model model) {
        CartService.CartView view = cartService.getCartView(session);
        model.addAttribute("lines", view.lines());
        model.addAttribute("total", view.total());
        return "cart";
    }
}
