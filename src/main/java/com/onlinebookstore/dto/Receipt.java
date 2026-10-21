package com.onlinebookstore.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Receipt implements Serializable {

    private List<ReceiptLine> lines;
    private BigDecimal total;
    private LocalDateTime placedAt;

    public List<ReceiptLine> getLines() {
        return lines;
    }

    public void setLines(List<ReceiptLine> lines) {
        this.lines = lines;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(LocalDateTime placedAt) {
        this.placedAt = placedAt;
    }
}
