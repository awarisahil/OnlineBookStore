package com.onlinebookstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.onlinebookstore.service.CatalogService;

@Controller
public class HomeController {

    private final CatalogService catalogService;

    public HomeController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("featured", catalogService.featured(4));
        model.addAttribute("trending", catalogService.bestsellers(4));
        model.addAttribute("categories", catalogService.browse(
                new CatalogService.CatalogFilter(null, null, null, "title", 0)).categories());
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/faq")
    public String faq() {
        return "faq";
    }
}
