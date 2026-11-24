package com.onlinebookstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.onlinebookstore.model.Category;
import com.onlinebookstore.service.AuditService;
import com.onlinebookstore.service.CategoryService;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final AuditService auditService;

    public AdminCategoryController(CategoryService categoryService, AuditService auditService) {
        this.categoryService = categoryService;
        this.auditService = auditService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.all());
        if (!model.containsAttribute("category")) {
            model.addAttribute("category", new Category());
        }
        return "admin/categories";
    }

    @PostMapping
    public String create(Category category, RedirectAttributes redirectAttributes) {
        try {
            categoryService.save(category);
            redirectAttributes.addFlashAttribute("successMessage", "Category added.");
            auditService.record("admin", "CATEGORY_CREATE", category.getName());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category removed.");
            auditService.record("admin", "CATEGORY_DELETE", "id " + id);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "That category is still used by books — reassign them first.");
        }
        return "redirect:/admin/categories";
    }
}
