package com.onlinebookstore.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Renders real error pages with their original HTTP status (the legacy app
 * returned 200 for every error).
 */
@Controller
public class AppErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int code = status instanceof Integer ? (int) status : 500;
        model.addAttribute("status", code);
        if (code == 404) {
            return "error/404";
        }
        if (code == 403) {
            return "error/403";
        }
        return "error/500";
    }

    @RequestMapping("/error/403")
    public String forbidden() {
        return "error/403";
    }
}
