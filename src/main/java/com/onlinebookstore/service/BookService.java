package com.onlinebookstore.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onlinebookstore.exception.BookNotFoundException;
import com.onlinebookstore.exception.OutOfStockException;
import com.onlinebookstore.model.Book;
import com.onlinebookstore.model.CartLine;
import com.onlinebookstore.repository.BookRepository;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Book> search(String query) {
        if (query == null || query.isBlank()) {
            return bookRepository.findAll();
        }
        String trimmed = query.trim();
        return bookRepository.findByNameContainingIgnoreCaseOrAuthorContainingIgnoreCase(trimmed, trimmed);
    }

    @Transactional(readOnly = true)
    public Book findByBarcode(String barcode) {
        return bookRepository.findById(barcode).orElseThrow(() -> new BookNotFoundException(barcode));
    }

    @Transactional
    public Book create(Book book) {
        book.setBarcode(UUID.randomUUID().toString());
        return bookRepository.save(book);
    }

    @Transactional
    public Book update(Book form) {
        Book book = findByBarcode(form.getBarcode());
        book.setName(form.getName());
        book.setAuthor(form.getAuthor());
        book.setPrice(form.getPrice());
        book.setQuantity(form.getQuantity());
        return bookRepository.save(book);
    }

    @Transactional
    public void delete(String barcode) {
        bookRepository.delete(findByBarcode(barcode));
    }

    /**
     * Decrease stock for one cart line atomically; throws OutOfStockException when
     * the available quantity is not sufficient.
     */
    @Transactional
    public void purchaseLine(CartLine line) {
        if (bookRepository.decrementStock(line.getBarcode(), line.getQuantity()) == 0) {
            throw new OutOfStockException(line.getBarcode());
        }
    }

    /**
     * Purchase all cart items in a single transaction. Each stock decrement is an
     * atomic conditional UPDATE, so concurrent buyers cannot oversell; a failure
     * rolls back the whole purchase.
     */
    @Transactional
    public void purchase(Map<String, Integer> items) {
        items.forEach((barcode, quantity) -> {
            if (bookRepository.decrementStock(barcode, quantity) == 0) {
                throw new OutOfStockException(barcode);
            }
        });
    }
}
