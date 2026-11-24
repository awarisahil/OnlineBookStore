package com.onlinebookstore.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onlinebookstore.model.PromoCode;
import com.onlinebookstore.repository.PromoCodeRepository;

@Service
public class PromoService {

    private final PromoCodeRepository promoCodeRepository;

    public PromoService(PromoCodeRepository promoCodeRepository) {
        this.promoCodeRepository = promoCodeRepository;
    }

    public record PromoResult(PromoCode promo, BigDecimal discount, String error) {
        public boolean applied() {
            return error == null;
        }
    }

    @Transactional(readOnly = true)
    public Optional<PromoCode> find(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return promoCodeRepository.findById(code.trim().toUpperCase());
    }

    /** Validate a code against a subtotal without consuming it. */
    @Transactional(readOnly = true)
    public PromoResult validate(String code, BigDecimal subtotal) {
        if (code == null || code.isBlank()) {
            return new PromoResult(null, BigDecimal.ZERO, null);
        }
        Optional<PromoCode> promo = find(code);
        if (promo.isEmpty()) {
            return new PromoResult(null, BigDecimal.ZERO, "That promo code does not exist.");
        }
        PromoCode p = promo.get();
        if (!p.isUsable()) {
            return new PromoResult(null, BigDecimal.ZERO, "That promo code is no longer valid.");
        }
        if (subtotal.compareTo(p.getMinOrder()) < 0) {
            return new PromoResult(null, BigDecimal.ZERO,
                    "This code needs a minimum order of ₹" + p.getMinOrder() + ".");
        }
        return new PromoResult(p, p.discountFor(subtotal), null);
    }

    /** Consume one use of a code (called when an order is placed). */
    @Transactional
    public void consume(String code) {
        if (code == null || code.isBlank()) {
            return;
        }
        promoCodeRepository.findById(code.trim().toUpperCase()).ifPresent(p -> {
            p.setUsedCount(p.getUsedCount() + 1);
        });
    }

    @Transactional(readOnly = true)
    public List<PromoCode> all() {
        return promoCodeRepository.findAll();
    }

    @Transactional
    public PromoCode save(PromoCode promo) {
        promo.setCode(promo.getCode().trim().toUpperCase());
        return promoCodeRepository.save(promo);
    }

    @Transactional
    public void delete(String code) {
        promoCodeRepository.deleteById(code);
    }
}
