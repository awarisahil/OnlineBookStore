package com.onlinebookstore.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onlinebookstore.exception.BookNotFoundException;
import com.onlinebookstore.model.Book;
import com.onlinebookstore.model.Review;
import com.onlinebookstore.model.User;
import com.onlinebookstore.repository.BookRepository;
import com.onlinebookstore.repository.ReviewRepository;
import com.onlinebookstore.repository.UserRepository;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, BookRepository bookRepository,
            UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public record BookRating(double average, long count) {
        public static final BookRating NONE = new BookRating(0, 0);
    }

    @Transactional(readOnly = true)
    public List<Review> visibleForBook(String bookId) {
        return reviewRepository.findByBookBarcodeAndHiddenFalseOrderByCreatedAtDesc(bookId);
    }

    @Transactional(readOnly = true)
    public BookRating ratingFor(String bookId) {
        Double avg = reviewRepository.averageRatingFor(bookId);
        long count = reviewRepository.countByBookBarcodeAndHiddenFalse(bookId);
        if (avg == null) {
            return BookRating.NONE;
        }
        return new BookRating(Math.round(avg * 10) / 10.0, count);
    }

    @Transactional
    public Review addOrUpdate(String bookId, String customerEmail, int rating, String text) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));
        User customer = userRepository.findById(customerEmail).orElseThrow();
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        return reviewRepository.findByBookBarcodeAndCustomerEmail(bookId, customerEmail)
                .map(existing -> {
                    existing.setRating(rating);
                    existing.setText(text);
                    return existing;
                })
                .orElseGet(() -> reviewRepository.save(new Review(book, customer, rating, text)));
    }

    @Transactional(readOnly = true)
    public List<Review> all() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void setHidden(Long id, boolean hidden) {
        Review review = reviewRepository.findById(id).orElseThrow();
        review.setHidden(hidden);
    }

    @Transactional
    public void delete(Long id) {
        reviewRepository.deleteById(id);
    }
}
