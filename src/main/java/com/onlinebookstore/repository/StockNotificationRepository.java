package com.onlinebookstore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onlinebookstore.model.StockNotification;

public interface StockNotificationRepository extends JpaRepository<StockNotification, Long> {

    List<StockNotification> findByBookIdAndNotifiedFalse(String bookId);

    boolean existsByBookIdAndEmail(String bookId, String email);
}
