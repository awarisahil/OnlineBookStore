package com.onlinebookstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.onlinebookstore.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBookIdAndHiddenFalseOrderByCreatedAtDesc(String bookId);

    List<Review> findByBookIdOrderByCreatedAtDesc(String bookId);

    List<Review> findAllByOrderByCreatedAtDesc();

    Optional<Review> findByBookIdAndCustomerEmail(String bookId, String customerEmail);

    long countByBookIdAndHiddenFalse(String bookId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId AND r.hidden = false")
    Double averageRatingFor(@Param("bookId") String bookId);
}
