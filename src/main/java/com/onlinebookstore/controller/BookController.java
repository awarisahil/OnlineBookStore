package com.onlinebookstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.onlinebookstore.service.BookService;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String list(@RequestParam(name = "q", required = false) String query, Model model) {
        model.addAttribute("books", bookService.search(query));
        model.addAttribute("q", query == null ? "" : query);
        return "books";
    }
}
