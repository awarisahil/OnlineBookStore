package com.onlinebookstore.exception;

public class OutOfStockException extends RuntimeException {

    private final String barcode;

    public OutOfStockException(String barcode) {
        super("Not enough copies in stock for book " + barcode);
        this.barcode = barcode;
    }

    public String getBarcode() {
        return barcode;
    }
}
