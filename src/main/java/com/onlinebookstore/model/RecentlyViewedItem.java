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
@Table(name = "recently_viewed")
@IdClass(RecentlyViewedItem.ViewedKey.class)
public class RecentlyViewedItem {

    @Id
    @Column(name = "customer_email", nullable = false, length = 200)
    private String customerEmail;

    @Id
    @Column(name = "book_id", nullable = false, length = 100)
    private String bookId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", insertable = false, updatable = false)
    private Book book;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt = LocalDateTime.now();

    public RecentlyViewedItem() {
    }

    public RecentlyViewedItem(String customerEmail, String bookId) {
        this.customerEmail = customerEmail;
        this.bookId = bookId;
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

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }

    public void touch() {
        this.viewedAt = LocalDateTime.now();
    }

    public static class ViewedKey implements Serializable {
        private String customerEmail;
        private String bookId;

        public ViewedKey() {
        }

        public ViewedKey(String customerEmail, String bookId) {
            this.customerEmail = customerEmail;
            this.bookId = bookId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ViewedKey other)) {
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
