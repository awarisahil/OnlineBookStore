package com.onlinebookstore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.onlinebookstore.model.Book;
import com.onlinebookstore.model.Review;
import com.onlinebookstore.model.Role;
import com.onlinebookstore.model.User;
import com.onlinebookstore.repository.BookRepository;
import com.onlinebookstore.repository.ReviewRepository;
import com.onlinebookstore.repository.UserRepository;

/**
 * Seeds the two demo accounts on first start, hashing passwords with the
 * application's encoder. Books and promo codes are seeded by Flyway (V2, V4);
 * this runner adds a few sample reviews and featured flags on top.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, BookRepository bookRepository,
            ReviewRepository reviewRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            seedReviewsAndFeaturedFlags();
            return;
        }
        User admin = userRepository.save(new User("admin@bookstore.local", passwordEncoder.encode("admin123"),
                "Store", "Admin", "Bookstore HQ", "1000000000", Role.SELLER));
        User demo = userRepository.save(new User("demo@bookstore.local", passwordEncoder.encode("demo123"),
                "Demo", "User", "123 Reader Lane", "2000000000", Role.CUSTOMER));
        log.info("Seeded demo accounts: admin@bookstore.local (seller), demo@bookstore.local (customer)");
        seedReviewsAndFeaturedFlags();
        log.debug("Seeded sample content for {}", demo.getEmail());
        log.debug("Seeded sample content for {}", admin.getEmail());
    }

    private void seedReviewsAndFeaturedFlags() {
        User demo = userRepository.findById("demo@bookstore.local").orElse(null);
        if (demo == null || reviewRepository.count() > 0) {
            return;
        }
        review("9780134190500", demo, 5,
                "Worked through every chapter before my interview prep — the C implementations make the concepts stick.");
        review("9780132350884", demo, 5,
                "The chapter on meaningful names alone is worth the price. My team has a shared copy now.");
        review("9781491910740", demo, 4,
                "Quirky but genuinely effective. The visuals feel silly at first, then you realize you remember everything.");
        review("9781492056300", demo, 5,
                "Not a beginner book, but if you already write Python this will level up your style.");
        review("9781718500457", demo, 4,
                "The ownership chapters are a tough climb, but the examples carry you through.");
        markFeatured("9780134190500");
        markFeatured("9781491910740");
        markFeatured("9780316204552");
        markFeatured("9780132350884");
    }

    private void review(String barcode, User demo, int rating, String text) {
        bookRepository.findById(barcode).ifPresent(book -> {
            bookRepository.save(book);
            reviewRepository.save(new Review(book, demo, rating, text));
        });
    }

    private void markFeatured(String barcode) {
        bookRepository.findById(barcode).ifPresent(book -> {
            book.setFeatured(true);
            bookRepository.save(book);
        });
    }
}
