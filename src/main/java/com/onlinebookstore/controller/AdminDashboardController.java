package com.onlinebookstore.controller;

import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.onlinebookstore.model.Book;
import com.onlinebookstore.model.ShopOrder;
import com.onlinebookstore.model.User;
import com.onlinebookstore.service.BookService;
import com.onlinebookstore.service.CatalogService;
import com.onlinebookstore.service.OrderService;
import com.onlinebookstore.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final OrderService orderService;
    private final UserService userService;
    private final BookService bookService;
    private final CatalogService catalogService;

    public AdminDashboardController(OrderService orderService, UserService userService, BookService bookService,
            CatalogService catalogService) {
        this.orderService = orderService;
        this.userService = userService;
        this.bookService = bookService;
        this.catalogService = catalogService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("orderCount", orderService.nonCancelledCount());
        model.addAttribute("revenue", orderService.totalRevenue());
        model.addAttribute("copiesSold", orderService.copiesSold());
        model.addAttribute("customerCount", userService.count());
        model.addAttribute("bookCount", bookService.findAll().size());
        model.addAttribute("lowStock", catalogService.lowStock(5));
        model.addAttribute("recentOrders", orderService.recent());
        model.addAttribute("bestsellers", catalogService.bestsellers(5));
        return "admin/dashboard";
    }

    @GetMapping("/export/orders.csv")
    public void exportOrders(HttpServletResponse response) throws Exception {
        prepareCsv(response, "orders.csv");
        PrintWriter out = response.getWriter();
        out.println("Order ID,Customer,Placed,Status,Promo,Discount,Total");
        for (ShopOrder order : orderService.findAll()) {
            out.printf("%d,%s,%s,%s,%s,%s,%s%n",
                    order.getId(),
                    csv(order.getCustomer().getEmail()),
                    order.getPlacedAt(),
                    order.getStatus(),
                    order.getPromoCode() == null ? "" : csv(order.getPromoCode()),
                    order.getDiscount(),
                    order.getTotal());
        }
    }

    @GetMapping("/export/books.csv")
    public void exportBooks(HttpServletResponse response) throws Exception {
        prepareCsv(response, "books.csv");
        PrintWriter out = response.getWriter();
        out.println("Barcode,Name,Author,Category,Price,Stock");
        for (Book book : bookService.findAll()) {
            out.printf("%s,%s,%s,%s,%s,%d%n",
                    csv(book.getBarcode()),
                    csv(book.getName()),
                    csv(book.getAuthor()),
                    csv(book.getCategory() == null ? "" : book.getCategory().getName()),
                    book.getPrice(),
                    book.getQuantity());
        }
    }

    @GetMapping("/export/customers.csv")
    public void exportCustomers(HttpServletResponse response) throws Exception {
        prepareCsv(response, "customers.csv");
        PrintWriter out = response.getWriter();
        out.println("Email,First name,Last name,Phone,Role,Enabled");
        List<User> users = userService.all();
        for (User user : users) {
            out.printf("%s,%s,%s,%s,%s,%s%n",
                    csv(user.getEmail()),
                    csv(user.getFirstName()),
                    csv(user.getLastName()),
                    csv(user.getPhone()),
                    user.getRole(),
                    user.isEnabled());
        }
    }

    private void prepareCsv(HttpServletResponse response, String filename) {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        String v = value.replace("\"", "\"\"");
        return v.contains(",") || v.contains("\"") ? "\"" + v + "\"" : v;
    }
}
