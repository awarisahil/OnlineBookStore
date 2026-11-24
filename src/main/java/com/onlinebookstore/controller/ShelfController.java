package com.onlinebookstore.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.onlinebookstore.model.Book;
import com.onlinebookstore.service.CatalogService;
import com.onlinebookstore.service.ReviewService;

@Controller
public class ShelfController {

    private final CatalogService catalogService;
    private final ReviewService reviewService;

    public ShelfController(CatalogService catalogService, ReviewService reviewService) {
        this.catalogService = catalogService;
        this.reviewService = reviewService;
    }

    @GetMapping("/trending")
    public String trending(Model model) {
        List<Book> books = catalogService.bestsellers(12);
        model.addAttribute("heading", "Trending this week");
        model.addAttribute("subheading", "The books everyone is ordering right now.");
        model.addAttribute("books", books);
        model.addAttribute("ratings", ratingsFor(books));
        return "shelf";
    }

    @GetMapping("/new-arrivals")
    public String newArrivals(Model model) {
        List<Book> books = catalogService.newArrivals(12);
        model.addAttribute("heading", "New arrivals");
        model.addAttribute("subheading", "Fresh additions to the catalog.");
        model.addAttribute("books", books);
        model.addAttribute("ratings", ratingsFor(books));
        return "shelf";
    }

    private Map<String, ReviewService.BookRating> ratingsFor(List<Book> books) {
        Map<String, ReviewService.BookRating> ratings = new HashMap<>();
        for (Book book : books) {
            ratings.put(book.getBarcode(), reviewService.ratingFor(book.getBarcode()));
        }
        return ratings;
    }
}
