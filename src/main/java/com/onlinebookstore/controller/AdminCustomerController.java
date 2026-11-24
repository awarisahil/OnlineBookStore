package com.onlinebookstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.onlinebookstore.model.Role;
import com.onlinebookstore.service.AuditService;
import com.onlinebookstore.service.OrderService;
import com.onlinebookstore.service.UserService;

@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    private final UserService userService;
    private final OrderService orderService;
    private final AuditService auditService;

    public AdminCustomerController(UserService userService, OrderService orderService, AuditService auditService) {
        this.userService = userService;
        this.orderService = orderService;
        this.auditService = auditService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("customers", userService.all());
        return "admin/customers";
    }

    @GetMapping("/{email}")
    public String detail(@PathVariable String email, Model model) {
        model.addAttribute("customer", userService.findByEmail(email));
        model.addAttribute("orders", orderService.findForCustomer(email));
        return "admin/customer-detail";
    }

    @PostMapping("/{email}/role")
    public String toggleRole(@PathVariable String email, RedirectAttributes redirectAttributes) {
        var user = userService.findByEmail(email);
        Role newRole = user.getRole() == Role.SELLER ? Role.CUSTOMER : Role.SELLER;
        userService.setRole(email, newRole);
        auditService.record("admin", "CUSTOMER_ROLE", email + " -> " + newRole);
        redirectAttributes.addFlashAttribute("successMessage",
                email + " is now a " + newRole.name().toLowerCase() + ".");
        return "redirect:/admin/customers/" + email;
    }

    @PostMapping("/{email}/enable")
    public String toggleEnabled(@PathVariable String email, RedirectAttributes redirectAttributes) {
        var user = userService.findByEmail(email);
        userService.setEnabled(email, !user.isEnabled());
        auditService.record("admin", "CUSTOMER_ENABLED", email + " -> " + !user.isEnabled());
        redirectAttributes.addFlashAttribute("successMessage",
                email + (user.isEnabled() ? " has been disabled." : " has been enabled."));
        return "redirect:/admin/customers/" + email;
    }
}
