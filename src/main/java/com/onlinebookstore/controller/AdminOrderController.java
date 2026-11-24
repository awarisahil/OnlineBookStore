package com.onlinebookstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.onlinebookstore.model.OrderStatus;
import com.onlinebookstore.service.AuditService;
import com.onlinebookstore.service.OrderService;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;
    private final AuditService auditService;

    public AdminOrderController(OrderService orderService, AuditService auditService) {
        this.orderService = orderService;
        this.auditService = auditService;
    }

    @GetMapping
    public String list(@RequestParam(name = "status", required = false) String status, Model model) {
        OrderStatus filter = parseStatus(status);
        model.addAttribute("orders", orderService.findByStatus(filter));
        model.addAttribute("status", status == null ? "" : status);
        return "admin/orders";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.findById(id));
        return "admin/order-detail";
    }

    @PostMapping("/{id}/advance")
    public String advance(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.advance(id);
            OrderStatus status = orderService.findById(id).getStatus();
            redirectAttributes.addFlashAttribute("successMessage",
                    "Order #" + id + " is now " + status + ".");
            auditService.record("admin", "ORDER_ADVANCE", "Order #" + id + " -> " + status);
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, @RequestParam String reason,
            RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelBySeller(id, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Order #" + id + " cancelled — stock returned.");
            auditService.record("admin", "ORDER_CANCEL", "Order #" + id + ": " + reason);
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    private OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
