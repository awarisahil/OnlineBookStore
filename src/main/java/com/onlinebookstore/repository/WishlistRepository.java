package com.onlinebookstore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.onlinebookstore.model.WishlistItem;

public interface WishlistRepository extends JpaRepository<WishlistItem, WishlistItem.WishlistKey> {

    @EntityGraph(attributePaths = { "book" })
    List<WishlistItem> findByCustomerEmailOrderByAddedAtDesc(String customerEmail);

    boolean existsByCustomerEmailAndBookId(String customerEmail, String bookId);

    void deleteByCustomerEmailAndBookId(String customerEmail, String bookId);
}
