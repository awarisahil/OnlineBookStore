package com.onlinebookstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.onlinebookstore.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = { "customer" })
    List<Review> findByBookBarcodeAndHiddenFalseOrderByCreatedAtDesc(String barcode);

    List<Review> findByBookBarcodeOrderByCreatedAtDesc(String barcode);

    List<Review> findAllByOrderByCreatedAtDesc();

    Optional<Review> findByBookBarcodeAndCustomerEmail(String barcode, String customerEmail);

    long countByBookBarcodeAndHiddenFalse(String barcode);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.barcode = :barcode AND r.hidden = false")
    Double averageRatingFor(@Param("barcode") String barcode);
}
