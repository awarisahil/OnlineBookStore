package com.onlinebookstore.controller;

import jakarta.servlet.http.HttpSession;

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
import com.onlinebookstore.service.BookService;
import com.onlinebookstore.service.CartService;
import com.onlinebookstore.service.PromoService;

@Controller
@RequestMapping("/cart")
public class CartController {

    static final String PROMO_SESSION_KEY = "promoCode";

    private final CartService cartService;
    private final BookService bookService;
    private final PromoService promoService;

    public CartController(CartService cartService, BookService bookService, PromoService promoService) {
        this.cartService = cartService;
        this.bookService = bookService;
        this.promoService = promoService;
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
        model.addAttribute("subtotal", view.total());

        String promoCode = promoCode(session);
        PromoService.PromoResult promo = promoService.validate(promoCode, view.total());
        if (promo.applied() && promo.promo() != null) {
            model.addAttribute("promo", promo.promo());
            model.addAttribute("discount", promo.discount());
            model.addAttribute("total", view.total().subtract(promo.discount()));
        } else {
            if (promo.error() != null) {
                model.addAttribute("promoError", promo.error());
            }
            model.addAttribute("total", view.total());
        }
        return "cart";
    }

    @PostMapping("/promo")
    public String applyPromo(@RequestParam String code, HttpSession session,
            RedirectAttributes redirectAttributes) {
        CartService.CartView view = cartService.getCartView(session);
        PromoService.PromoResult result = promoService.validate(code, view.total());
        if (result.applied()) {
            session.setAttribute(PROMO_SESSION_KEY, result.promo().getCode());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Promo code " + result.promo().getCode() + " applied — you save ₹" + result.discount() + ".");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", result.error());
        }
        return "redirect:/cart";
    }

    @PostMapping("/promo/remove")
    public String removePromo(HttpSession session) {
        session.removeAttribute(PROMO_SESSION_KEY);
        return "redirect:/cart";
    }

    static String promoCode(HttpSession session) {
        Object value = session.getAttribute(PROMO_SESSION_KEY);
        return value instanceof String ? (String) value : null;
    }
}
