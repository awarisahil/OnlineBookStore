package com.onlinebookstore.exception;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(String barcode) {
        super("No book found with barcode " + barcode);
    }
}
