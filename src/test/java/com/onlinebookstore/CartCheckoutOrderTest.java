package com.onlinebookstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.onlinebookstore.dto.Receipt;
import com.onlinebookstore.model.Book;
import com.onlinebookstore.model.Cart;
import com.onlinebookstore.repository.BookRepository;
import com.onlinebookstore.repository.OrderRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CartCheckoutOrderTest {

    static final String CLEAN_CODE = "9780132350884"; // seeded quantity: 3, price 288

    @Autowired
    MockMvc mockMvc;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    OrderRepository orderRepository;

    @Test
    @WithMockUser(username = "demo@bookstore.local", roles = "CUSTOMER")
    void fullPurchaseFlowDecrementsStockAndPersistsOrder() throws Exception {
        MockHttpSession session = new MockHttpSession();
        int stockBefore = bookRepository.findById(CLEAN_CODE).orElseThrow().getQuantity();
        long ordersBefore = orderRepository.count();

        mockMvc.perform(post("/cart/add/{barcode}", CLEAN_CODE).with(csrf()).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
        mockMvc.perform(post("/cart/inc/{barcode}", CLEAN_CODE).with(csrf()).session(session))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/cart").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("576.00")));

        mockMvc.perform(get("/checkout").session(session)).andExpect(status().isOk());
        MvcResult paid = mockMvc.perform(post("/pay").with(csrf()).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/receipt"))
                .andReturn();

        Book book = bookRepository.findById(CLEAN_CODE).orElseThrow();
        assertThat(book.getQuantity()).isEqualTo(stockBefore - 2);

        Receipt receipt = (Receipt) paid.getFlashMap().get("receipt");
        assertThat(receipt).isNotNull();
        var order = orderRepository.findWithDetailsById(receipt.getOrderId()).orElseThrow();
        assertThat(order.getCustomer().getEmail()).isEqualTo("demo@bookstore.local");
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(order.getTotal()).isEqualByComparingTo(new BigDecimal("576.00"));
        assertThat(orderRepository.count()).isEqualTo(ordersBefore + 1);

        // receipt renders from the flash attribute on the redirected GET
        mockMvc.perform(get("/receipt").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("576.00")));

        // cart cleared: paying again redirects to the cart page
        mockMvc.perform(post("/pay").with(csrf()).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    @WithMockUser(username = "demo@bookstore.local", roles = "CUSTOMER")
    void purchaseWithInsufficientStockRenders409AndRollsBack() throws Exception {
        int stockBefore = bookRepository.findById(CLEAN_CODE).orElseThrow().getQuantity();
        long ordersBefore = orderRepository.count();

        MockHttpSession session = new MockHttpSession();
        Cart cart = new Cart();
        for (int i = 0; i < stockBefore + 1; i++) {
            cart.add(CLEAN_CODE); // demand one more than the available stock
        }
        session.setAttribute("cart", cart);

        mockMvc.perform(post("/pay").with(csrf()).session(session))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Not enough stock")));

        // nothing was persisted and stock unchanged
        assertThat(orderRepository.count()).isEqualTo(ordersBefore);
        assertThat(bookRepository.findById(CLEAN_CODE).orElseThrow().getQuantity()).isEqualTo(stockBefore);
    }

    @Test
    @WithMockUser(username = "demo@bookstore.local", roles = "CUSTOMER")
    void cannotAddMoreToCartThanAvailable() throws Exception {
        int stock = bookRepository.findById(CLEAN_CODE).orElseThrow().getQuantity();
        MockHttpSession session = new MockHttpSession();
        MvcResult last = null;
        for (int i = 0; i < stock + 2; i++) {
            mockMvc.perform(post("/cart/add/{barcode}", CLEAN_CODE).with(csrf()).session(session))
                    .andExpect(status().is3xxRedirection());
            // consume the flash message after each redirect so the newest one is visible
            last = mockMvc.perform(get("/books").session(session))
                    .andExpect(status().isOk())
                    .andReturn();
        }
        assertThat(last.getResponse().getContentAsString())
                .contains("Only " + stock + " copies");
    }
}
