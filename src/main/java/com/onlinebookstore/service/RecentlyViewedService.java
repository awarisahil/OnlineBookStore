package com.onlinebookstore.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onlinebookstore.model.RecentlyViewedItem;
import com.onlinebookstore.repository.RecentlyViewedRepository;

@Service
public class RecentlyViewedService {

    private final RecentlyViewedRepository recentlyViewedRepository;

    public RecentlyViewedService(RecentlyViewedRepository recentlyViewedRepository) {
        this.recentlyViewedRepository = recentlyViewedRepository;
    }

    @Transactional
    public void record(String customerEmail, String bookId) {
        RecentlyViewedItem item = recentlyViewedRepository
                .findById(new RecentlyViewedItem.ViewedKey(customerEmail, bookId))
                .orElseGet(() -> new RecentlyViewedItem(customerEmail, bookId));
        item.touch();
        recentlyViewedRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<RecentlyViewedItem> forCustomer(String customerEmail) {
        return recentlyViewedRepository.findTop8ByCustomerEmailOrderByViewedAtDesc(customerEmail);
    }
}
