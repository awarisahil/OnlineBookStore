package com.onlinebookstore.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onlinebookstore.model.Book;
import com.onlinebookstore.model.StockNotification;
import com.onlinebookstore.repository.StockNotificationRepository;

/**
 * "Notify me when it's back" sign-ups. Emails are simulated by logging the
 * message that would be sent; there is no SMTP server in this demo app.
 */
@Service
public class StockNotificationService {

    private static final Logger log = LoggerFactory.getLogger(StockNotificationService.class);

    private final StockNotificationRepository stockNotificationRepository;

    public StockNotificationService(StockNotificationRepository stockNotificationRepository) {
        this.stockNotificationRepository = stockNotificationRepository;
    }

    @Transactional
    public boolean register(String bookId, String email) {
        if (stockNotificationRepository.existsByBookIdAndEmail(bookId, email)) {
            return false;
        }
        stockNotificationRepository.save(new StockNotification(bookId, email));
        return true;
    }

    @Transactional
    public void notifyRestocked(Book book) {
        List<StockNotification> pending = stockNotificationRepository.findByBookIdAndNotifiedFalse(book.getBarcode());
        for (StockNotification notification : pending) {
            notification.setNotified(true);
            log.info("MAIL (simulated) -> {}: '{}' is back in stock ({} copies).", notification.getEmail(),
                    book.getName(), book.getQuantity());
        }
    }

    @Transactional(readOnly = true)
    public List<StockNotification> pending() {
        return stockNotificationRepository.findAll().stream().filter(n -> !n.isNotified()).toList();
    }
}
