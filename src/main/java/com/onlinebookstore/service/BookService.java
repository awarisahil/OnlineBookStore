package com.onlinebookstore.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onlinebookstore.exception.BookNotFoundException;
import com.onlinebookstore.exception.OutOfStockException;
import com.onlinebookstore.model.Book;
import com.onlinebookstore.model.CartLine;
import com.onlinebookstore.repository.BookRepository;
import com.onlinebookstore.repository.CategoryRepository;
import com.onlinebookstore.repository.ReviewRepository;
import com.onlinebookstore.repository.StockNotificationRepository;
import com.onlinebookstore.repository.WishlistRepository;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final WishlistRepository wishlistRepository;
    private final StockNotificationRepository stockNotificationRepository;

    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository,
            ReviewRepository reviewRepository, WishlistRepository wishlistRepository,
            StockNotificationRepository stockNotificationRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.reviewRepository = reviewRepository;
        this.wishlistRepository = wishlistRepository;
        this.stockNotificationRepository = stockNotificationRepository;
    }

    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Book> search(String query) {
        if (query == null || query.isBlank()) {
            return bookRepository.findAll();
        }
        String trimmed = query.trim();
        return bookRepository.findByNameContainingIgnoreCaseOrAuthorContainingIgnoreCase(trimmed, trimmed);
    }

    @Transactional(readOnly = true)
    public Book findByBarcode(String barcode) {
        return bookRepository.findById(barcode).orElseThrow(() -> new BookNotFoundException(barcode));
    }

    /** Detail-page lookup that eagerly loads the category for view rendering. */
    @Transactional(readOnly = true)
    public Book findDetailedByBarcode(String barcode) {
        return bookRepository.findByBarcodeWithCategory(barcode)
                .orElseThrow(() -> new BookNotFoundException(barcode));
    }

    @Transactional
    public Book create(Book book) {
        book.setBarcode(UUID.randomUUID().toString());
        attachCategory(book);
        return bookRepository.save(book);
    }

    @Transactional
    public Book update(Book form) {
        Book book = findByBarcode(form.getBarcode());
        book.setName(form.getName());
        book.setAuthor(form.getAuthor());
        book.setPrice(form.getPrice());
        book.setQuantity(form.getQuantity());
        book.setDescription(form.getDescription());
        book.setPublishedYear(form.getPublishedYear());
        book.setPages(form.getPages());
        book.setFeatured(form.isFeatured());
        book.setCategory(form.getCategory());
        attachCategory(book);
        return bookRepository.save(book);
    }

    private void attachCategory(Book book) {
        if (book.getCategory() != null && book.getCategory().getId() != null) {
            book.setCategory(categoryRepository.findById(book.getCategory().getId()).orElse(null));
        } else {
            book.setCategory(null);
        }
    }

    /** Remove a book together with its reviews, wishlist entries and stock alerts. */
    @Transactional
    public void delete(String barcode) {
        Book book = findByBarcode(barcode);
        reviewRepository.findByBookBarcodeOrderByCreatedAtDesc(barcode)
                .forEach(reviewRepository::delete);
        wishlistRepository.deleteByBookId(barcode);
        stockNotificationRepository.findByBookIdAndNotifiedFalse(barcode)
                .forEach(stockNotificationRepository::delete);
        bookRepository.delete(book);
    }

    @Transactional
    public void restock(String barcode, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Restock quantity must be positive");
        }
        bookRepository.incrementStock(barcode, quantity);
    }

    @Transactional
    public void setFeatured(String barcode, boolean featured) {
        findByBarcode(barcode).setFeatured(featured);
    }

    /**
     * Decrease stock for one cart line atomically; throws OutOfStockException when
     * the available quantity is not sufficient.
     */
    @Transactional
    public void purchaseLine(CartLine line) {
        if (bookRepository.decrementStock(line.getBarcode(), line.getQuantity()) == 0) {
            throw new OutOfStockException(line.getBarcode());
        }
    }

    /**
     * Purchase all cart items in a single transaction. Each stock decrement is an
     * atomic conditional UPDATE, so concurrent buyers cannot oversell; a failure
     * rolls back the whole purchase.
     */
    @Transactional
    public void purchase(Map<String, Integer> items) {
        items.forEach((barcode, quantity) -> {
            if (bookRepository.decrementStock(barcode, quantity) == 0) {
                throw new OutOfStockException(barcode);
            }
        });
    }
}
