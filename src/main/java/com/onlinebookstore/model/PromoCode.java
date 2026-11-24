package com.onlinebookstore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "promo_codes")
public class PromoCode {

    @Id
    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "percent_off", nullable = false)
    private int percentOff;

    @Column(name = "min_order", nullable = false, precision = 10, scale = 2)
    private BigDecimal minOrder = BigDecimal.ZERO;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "used_count", nullable = false)
    private int usedCount;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public PromoCode() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getPercentOff() {
        return percentOff;
    }

    public void setPercentOff(int percentOff) {
        this.percentOff = percentOff;
    }

    public BigDecimal getMinOrder() {
        return minOrder;
    }

    public void setMinOrder(BigDecimal minOrder) {
        this.minOrder = minOrder;
    }

    public Integer getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isUsable() {
        if (!active || (expiresAt != null && expiresAt.isBefore(LocalDateTime.now()))) {
            return false;
        }
        return maxUses == null || usedCount < maxUses;
    }

    public BigDecimal discountFor(BigDecimal subtotal) {
        return subtotal.multiply(BigDecimal.valueOf(percentOff))
                .divide(BigDecimal.valueOf(100))
                .stripTrailingZeros()
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
