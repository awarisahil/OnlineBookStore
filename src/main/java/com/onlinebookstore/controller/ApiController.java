package com.onlinebookstore.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onlinebookstore.model.Book;
import com.onlinebookstore.model.Category;
import com.onlinebookstore.model.Review;
import com.onlinebookstore.service.BookService;
import com.onlinebookstore.service.CatalogService;
import com.onlinebookstore.service.CategoryService;
import com.onlinebookstore.service.ReviewService;

/** Read-only JSON API used by the search autocomplete and available for integrations. */
@RestController
@RequestMapping("/api")
@Transactional(readOnly = true)
public class ApiController {

    private final CatalogService catalogService;
    private final BookService bookService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;

    public ApiController(CatalogService catalogService, BookService bookService, CategoryService categoryService,
            ReviewService reviewService) {
        this.catalogService = catalogService;
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
    }

    public record BookSummary(String barcode, String name, String author, String category, String price,
            int quantity, Double averageRating, long reviewCount) {
    }

    public record ReviewView(String author, int rating, String text, String createdAt) {
    }

    @GetMapping("/books")
    public List<BookSummary> books(@RequestParam(name = "q", required = false) String query) {
        List<Book> books = catalogService.suggest(query);
        return books.stream().map(this::summarize).collect(Collectors.toList());
    }

    @GetMapping("/books/{barcode}")
    public BookSummary book(@PathVariable String barcode) {
        return summarize(bookService.findByBarcode(barcode));
    }

    @GetMapping("/books/{barcode}/reviews")
    public List<ReviewView> reviews(@PathVariable String barcode) {
        return reviewService.visibleForBook(barcode).stream()
                .map(r -> new ReviewView(r.getCustomer().getFullName(), r.getRating(), r.getText(),
                        r.getCreatedAt().toLocalDate().toString()))
                .collect(Collectors.toList());
    }

    @GetMapping("/categories")
    public List<Map<String, Object>> categories() {
        return categoryService.all().stream()
                .map(c -> Map.<String, Object>of("id", c.getId(), "name", c.getName()))
                .collect(Collectors.toList());
    }

    private BookSummary summarize(Book book) {
        var rating = reviewService.ratingFor(book.getBarcode());
        Category category = book.getCategory();
        return new BookSummary(book.getBarcode(), book.getName(), book.getAuthor(),
                category == null ? null : category.getName(), book.getPrice().toPlainString(),
                book.getQuantity(),
                rating.count() == 0 ? null : rating.average(),
                rating.count());
    }
}
