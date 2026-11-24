package com.onlinebookstore.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.onlinebookstore.model.Book;
import com.onlinebookstore.service.BookService;
import com.onlinebookstore.service.CatalogService;
import com.onlinebookstore.service.RecentlyViewedService;
import com.onlinebookstore.service.ReviewService;
import com.onlinebookstore.service.StockNotificationService;
import com.onlinebookstore.service.WishlistService;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final CatalogService catalogService;
    private final ReviewService reviewService;
    private final WishlistService wishlistService;
    private final RecentlyViewedService recentlyViewedService;
    private final StockNotificationService stockNotificationService;

    public BookController(BookService bookService, CatalogService catalogService, ReviewService reviewService,
            WishlistService wishlistService, RecentlyViewedService recentlyViewedService,
            StockNotificationService stockNotificationService) {
        this.bookService = bookService;
        this.catalogService = catalogService;
        this.reviewService = reviewService;
        this.wishlistService = wishlistService;
        this.recentlyViewedService = recentlyViewedService;
        this.stockNotificationService = stockNotificationService;
    }

    @GetMapping
    public String list(@RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "category", required = false) Long categoryId,
            @RequestParam(name = "stock", required = false) String stock,
            @RequestParam(name = "sort", required = false, defaultValue = "title") String sort,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            Model model) {
        CatalogService.CatalogFilter filter = new CatalogService.CatalogFilter(
                query, categoryId, "in".equals(stock), sort, page);
        CatalogService.CatalogPage result = catalogService.browse(filter);

        model.addAttribute("result", result);
        model.addAttribute("books", result.books().getContent());
        model.addAttribute("q", query == null ? "" : query);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("stock", "in".equals(stock));
        model.addAttribute("sort", sort);
        model.addAttribute("ratings", ratingsFor(result.books().getContent()));
        return "books";
    }

    @GetMapping("/{barcode}")
    public String detail(@PathVariable String barcode, Principal principal, Model model) {
        Book book = bookService.findByBarcode(barcode);
        if (principal != null) {
            recentlyViewedService.record(principal.getName(), barcode);
        }
        List<Book> related = catalogService.relatedTo(book, 4);
        model.addAttribute("book", book);
        model.addAttribute("rating", reviewService.ratingFor(barcode));
        model.addAttribute("reviews", reviewService.visibleForBook(barcode));
        model.addAttribute("related", related);
        model.addAttribute("ratings", ratingsFor(related));
        if (principal != null) {
            model.addAttribute("onWishlist", wishlistService.contains(principal.getName(), barcode));
            model.addAttribute("recentlyViewed", recentlyViewedService.forCustomer(principal.getName()).stream()
                    .filter(item -> !item.getBookId().equals(barcode))
                    .limit(6)
                    .toList());
        }
        return "book-detail";
    }

    private Map<String, ReviewService.BookRating> ratingsFor(List<Book> books) {
        Map<String, ReviewService.BookRating> ratings = new HashMap<>();
        for (Book book : books) {
            ratings.put(book.getBarcode(), reviewService.ratingFor(book.getBarcode()));
        }
        return ratings;
    }
}
