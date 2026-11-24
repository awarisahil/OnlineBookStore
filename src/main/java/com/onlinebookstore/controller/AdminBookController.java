package com.onlinebookstore.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.onlinebookstore.model.Book;
import com.onlinebookstore.service.BookService;
import com.onlinebookstore.service.CategoryService;
import com.onlinebookstore.service.StockNotificationService;

@Controller
@RequestMapping("/admin/books")
public class AdminBookController {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final StockNotificationService stockNotificationService;

    public AdminBookController(BookService bookService, CategoryService categoryService,
            StockNotificationService stockNotificationService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.stockNotificationService = stockNotificationService;
    }

    @ModelAttribute
    public void categories(Model model) {
        model.addAttribute("categories", categoryService.all());
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("books", bookService.findAll());
        return "admin/books";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("book")) {
            model.addAttribute("book", new Book());
        }
        return "admin/book-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("book") Book book, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/book-form";
        }
        bookService.create(book);
        return "redirect:/admin/books?created";
    }

    @GetMapping("/{barcode}/edit")
    public String editForm(@PathVariable String barcode, Model model) {
        if (!model.containsAttribute("book")) {
            model.addAttribute("book", bookService.findByBarcode(barcode));
        }
        return "admin/book-form";
    }

    @PostMapping("/{barcode}")
    public String update(@PathVariable String barcode, @Valid @ModelAttribute("book") Book book,
            BindingResult bindingResult) {
        book.setBarcode(barcode);
        if (bindingResult.hasErrors()) {
            return "admin/book-form";
        }
        bookService.update(book);
        return "redirect:/admin/books?updated";
    }

    @PostMapping("/{barcode}/delete")
    public String delete(@PathVariable String barcode) {
        bookService.delete(barcode);
        return "redirect:/admin/books?deleted";
    }

    @PostMapping("/{barcode}/restock")
    public String restock(@PathVariable String barcode, @RequestParam(defaultValue = "10") int amount,
            RedirectAttributes redirectAttributes) {
        try {
            bookService.restock(barcode, amount);
            var book = bookService.findByBarcode(barcode);
            stockNotificationService.notifyRestocked(book);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Restocked \"" + book.getName() + "\" by " + amount + " — now " + book.getQuantity() + " in stock.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/books";
    }

    @PostMapping("/{barcode}/feature")
    public String feature(@PathVariable String barcode) {
        var book = bookService.findByBarcode(barcode);
        bookService.setFeatured(barcode, !book.isFeatured());
        return "redirect:/admin/books";
    }
}
