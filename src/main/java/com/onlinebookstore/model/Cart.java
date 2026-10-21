package com.onlinebookstore.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shopping cart stored in the HTTP session. One consolidated structure holding
 * book barcodes and quantities, replacing the legacy scheme of loosely-coupled
 * session keys.
 */
public class Cart implements Serializable {

    private final Map<String, Integer> lines = new LinkedHashMap<>();

    public void add(String barcode) {
        lines.merge(barcode, 1, Integer::sum);
    }

    public void increment(String barcode, int maxQuantity) {
        if (get(barcode) < maxQuantity) {
            add(barcode);
        }
    }

    public void decrement(String barcode) {
        Integer quantity = lines.get(barcode);
        if (quantity == null) {
            return;
        }
        if (quantity <= 1) {
            lines.remove(barcode);
        } else {
            lines.put(barcode, quantity - 1);
        }
    }

    public void remove(String barcode) {
        lines.remove(barcode);
    }

    public void clear() {
        lines.clear();
    }

    public int get(String barcode) {
        return lines.getOrDefault(barcode, 0);
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public Map<String, Integer> getLines() {
        return Collections.unmodifiableMap(lines);
    }
}
