package com.onlinebookstore;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.onlinebookstore.model.OrderItem;
import com.onlinebookstore.model.ShopOrder;
import com.onlinebookstore.model.User;
import com.onlinebookstore.repository.OrderRepository;
import com.onlinebookstore.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderHistoryTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void seedOrder() {
        orderRepository.deleteAll();
        User demo = userRepository.findById("demo@bookstore.local").orElseThrow();
        ShopOrder order = new ShopOrder();
        order.setCustomer(demo);
        order.setPlacedAt(LocalDateTime.now());
        order.addItem(new OrderItem("9780132350884", "Clean Code", new BigDecimal("288.00"), 2));
        order.setTotal(new BigDecimal("576.00"));
        orderRepository.save(order);
    }

    @Test
    @WithMockUser(username = "demo@bookstore.local", roles = "CUSTOMER")
    void customerSeesOwnOrderHistoryAndDetail() throws Exception {
        Long orderId = orderRepository.findAll().get(0).getId();

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("576.00")));

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Clean Code")));
    }

    @Test
    @WithMockUser(username = "someone-else@example.com", roles = "CUSTOMER")
    void otherCustomerCannotViewSomeoneElsesOrder() throws Exception {
        Long orderId = orderRepository.findAll().get(0).getId();
        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void sellerSeesSalesHistoryWithCustomerAndDetail() throws Exception {
        Long orderId = orderRepository.findAll().get(0).getId();

        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("demo@bookstore.local")));

        mockMvc.perform(get("/admin/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Clean Code")));
    }

    @Test
    @WithMockUser(username = "demo@bookstore.local", roles = "CUSTOMER")
    void customerIsForbiddenFromAdminOrders() throws Exception {
        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isForbidden());
    }
}
