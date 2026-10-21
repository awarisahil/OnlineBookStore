package com.onlinebookstore.model;

import java.math.BigDecimal;

/**
 * A single cart line resolved against current book data, used for display and
 * checkout.
 */
public class CartLine {

    private final String barcode;
    private final String name;
    private final BigDecimal price;
    private final int quantity;

    public CartLine(String barcode, String name, BigDecimal price, int quantity) {
        this.barcode = barcode;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getAmount() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
