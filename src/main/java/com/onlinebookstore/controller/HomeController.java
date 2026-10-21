package com.onlinebookstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.onlinebookstore.service.BookService;

@Controller
public class HomeController {

    private final BookService bookService;

    public HomeController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("featured", bookService.findAll().stream().limit(4).toList());
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
