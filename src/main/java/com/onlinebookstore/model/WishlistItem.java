package com.onlinebookstore.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "wishlist_items")
@IdClass(WishlistItem.WishlistKey.class)
public class WishlistItem {

    @Id
    @Column(name = "customer_email", nullable = false, length = 200)
    private String customerEmail;

    @Id
    @Column(name = "book_id", nullable = false, length = 100)
    private String bookId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", insertable = false, updatable = false)
    private Book book;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    public WishlistItem() {
    }

    public WishlistItem(String customerEmail, String bookId) {
        this.customerEmail = customerEmail;
        this.bookId = bookId;
        this.addedAt = LocalDateTime.now();
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getBookId() {
        return bookId;
    }

    public Book getBook() {
        return book;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public static class WishlistKey implements Serializable {
        private String customerEmail;
        private String bookId;

        public WishlistKey() {
        }

        public WishlistKey(String customerEmail, String bookId) {
            this.customerEmail = customerEmail;
            this.bookId = bookId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof WishlistKey other)) {
                return false;
            }
            return customerEmail.equals(other.customerEmail) && bookId.equals(other.bookId);
        }

        @Override
        public int hashCode() {
            return 31 * customerEmail.hashCode() + bookId.hashCode();
        }
    }
}
