package com.onlinebookstore.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.onlinebookstore.service.OrderService;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String list(Authentication authentication, Model model) {
        model.addAttribute("orders", orderService.findForCustomer(authentication.getName()));
        return "orders/index";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Authentication authentication, Model model) {
        model.addAttribute("order", orderService.findForCustomer(id, authentication.getName()));
        return "orders/detail";
    }
}
