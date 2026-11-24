package com.onlinebookstore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.onlinebookstore.model.RecentlyViewedItem;

public interface RecentlyViewedRepository extends JpaRepository<RecentlyViewedItem, RecentlyViewedItem.ViewedKey> {

    @EntityGraph(attributePaths = { "book" })
    List<RecentlyViewedItem> findTop8ByCustomerEmailOrderByViewedAtDesc(String customerEmail);
}
