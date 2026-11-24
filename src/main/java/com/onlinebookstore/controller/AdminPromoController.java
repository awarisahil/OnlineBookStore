package com.onlinebookstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.onlinebookstore.model.PromoCode;
import com.onlinebookstore.service.AuditService;
import com.onlinebookstore.service.PromoService;

@Controller
@RequestMapping("/admin/promos")
public class AdminPromoController {

    private final PromoService promoService;
    private final AuditService auditService;

    public AdminPromoController(PromoService promoService, AuditService auditService) {
        this.promoService = promoService;
        this.auditService = auditService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("promos", promoService.all());
        if (!model.containsAttribute("promo")) {
            model.addAttribute("promo", new PromoCode());
        }
        return "admin/promos";
    }

    @PostMapping
    public String create(PromoCode promo, RedirectAttributes redirectAttributes) {
        try {
            if (promo.getCode() == null || promo.getCode().isBlank()) {
                throw new IllegalArgumentException("A code is required.");
            }
            if (promo.getPercentOff() < 1 || promo.getPercentOff() > 90) {
                throw new IllegalArgumentException("Percent off must be between 1 and 90.");
            }
            promoService.save(promo);
            redirectAttributes.addFlashAttribute("successMessage", "Promo code saved.");
            auditService.record("admin", "PROMO_SAVE", promo.getCode());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/promos";
    }

    @PostMapping("/{code}/toggle")
    public String toggle(@PathVariable String code) {
        promoService.find(code).ifPresent(p -> {
            p.setActive(!p.isActive());
            promoService.save(p);
            auditService.record("admin", "PROMO_TOGGLE", code + " -> " + p.isActive());
        });
        return "redirect:/admin/promos";
    }

    @PostMapping("/{code}/delete")
    public String delete(@PathVariable String code, RedirectAttributes redirectAttributes) {
        promoService.delete(code);
        auditService.record("admin", "PROMO_DELETE", code);
        redirectAttributes.addFlashAttribute("successMessage", "Promo code deleted.");
        return "redirect:/admin/promos";
    }
}
