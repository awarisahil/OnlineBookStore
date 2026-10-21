package com.onlinebookstore.controller;

import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.onlinebookstore.exception.BookNotFoundException;
import com.onlinebookstore.exception.OutOfStockException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView notFound(NoResourceFoundException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        ModelAndView modelAndView = new ModelAndView("error/404");
        modelAndView.addObject("message", "The page you are looking for does not exist.");
        return modelAndView;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ModelAndView methodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpServletResponse response) {
        response.setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
        ModelAndView modelAndView = new ModelAndView("error/405");
        modelAndView.addObject("method", ex.getMethod());
        return modelAndView;
    }

    @ExceptionHandler(OutOfStockException.class)
    public ModelAndView outOfStock(OutOfStockException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.CONFLICT.value());
        ModelAndView modelAndView = new ModelAndView("error/out-of-stock");
        modelAndView.addObject("message", ex.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ModelAndView bookNotFound(BookNotFoundException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        ModelAndView modelAndView = new ModelAndView("error/404");
        modelAndView.addObject("message", ex.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView unexpected(Exception ex, HttpServletResponse response) {
        log.error("Unhandled exception", ex);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ModelAndView("error/500");
    }
}
