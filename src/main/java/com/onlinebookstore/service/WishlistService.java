package com.onlinebookstore.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onlinebookstore.model.WishlistItem;
import com.onlinebookstore.repository.WishlistRepository;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    public WishlistService(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    @Transactional(readOnly = true)
    public List<WishlistItem> forCustomer(String customerEmail) {
        return wishlistRepository.findByCustomerEmailOrderByAddedAtDesc(customerEmail);
    }

    @Transactional(readOnly = true)
    public boolean contains(String customerEmail, String bookId) {
        return wishlistRepository.existsByCustomerEmailAndBookId(customerEmail, bookId);
    }

    @Transactional
    public boolean toggle(String customerEmail, String bookId) {
        if (wishlistRepository.existsByCustomerEmailAndBookId(customerEmail, bookId)) {
            wishlistRepository.deleteByCustomerEmailAndBookId(customerEmail, bookId);
            return false;
        }
        wishlistRepository.save(new WishlistItem(customerEmail, bookId));
        return true;
    }

    @Transactional
    public void remove(String customerEmail, String bookId) {
        wishlistRepository.deleteByCustomerEmailAndBookId(customerEmail, bookId);
    }

    @Transactional(readOnly = true)
    public int count(String customerEmail) {
        return wishlistRepository.findByCustomerEmailOrderByAddedAtDesc(customerEmail).size();
    }
}
