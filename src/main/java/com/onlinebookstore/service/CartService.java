package com.onlinebookstore.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

import com.onlinebookstore.exception.BookNotFoundException;
import com.onlinebookstore.model.Book;
import com.onlinebookstore.model.Cart;
import com.onlinebookstore.model.CartLine;

@Service
public class CartService {

    private static final String SESSION_KEY = "cart";

    private final BookService bookService;

    public CartService(BookService bookService) {
        this.bookService = bookService;
    }

    public Cart getCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute(SESSION_KEY);
        if (cart == null) {
            cart = new Cart();
            session.setAttribute(SESSION_KEY, cart);
        }
        return cart;
    }

    /**
     * Resolve the session cart against current book data. Lines whose book no
     * longer exists are dropped, and the stale entries are cleaned from the cart.
     */
    public CartView getCartView(HttpSession session) {
        Cart cart = getCart(session);
        List<CartLine> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (var entry : cart.getLines().entrySet()) {
            try {
                Book book = bookService.findByBarcode(entry.getKey());
                CartLine line = new CartLine(book.getBarcode(), book.getName(), book.getPrice(), entry.getValue());
                lines.add(line);
                total = total.add(line.getAmount());
            } catch (BookNotFoundException ex) {
                cart.remove(entry.getKey());
            }
        }
        return new CartView(lines, total);
    }

    public record CartView(List<CartLine> lines, BigDecimal total) {
    }
}
