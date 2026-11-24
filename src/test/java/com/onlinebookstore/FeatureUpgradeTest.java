package com.onlinebookstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.onlinebookstore.model.Book;
import com.onlinebookstore.model.OrderStatus;
import com.onlinebookstore.repository.BookRepository;
import com.onlinebookstore.repository.OrderRepository;
import com.onlinebookstore.repository.PromoCodeRepository;
import com.onlinebookstore.service.ReviewService;
import com.onlinebookstore.service.StockNotificationService;
import com.onlinebookstore.service.WishlistService;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FeatureUpgradeTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    PromoCodeRepository promoCodeRepository;

    @Autowired
    ReviewService reviewService;

    @Autowired
    WishlistService wishlistService;

    @Autowired
    StockNotificationService stockNotificationService;

    @Autowired
    com.onlinebookstore.service.ContactService contactService;

    private static final String PROMO_BOOK = "9780316204552"; // The Soul of a New Machine, healthy stock
    private static final String REVIEW_BOOK = "9780134190563"; // The Go Programming Language

    @Test
    @Order(1)
    @WithMockUser(username = "demo@bookstore.local", roles = "CUSTOMER")
    void promoCodeDiscountsOrderAndIsPersisted() throws Exception {
        Book book = bookRepository.findById(PROMO_BOOK).orElseThrow();
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/cart/add/{barcode}", PROMO_BOOK).with(csrf()).session(session))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/cart/promo").param("code", "WELCOME10").with(csrf()).session(session))
                .andExpect(redirectedUrl("/cart"));
        mockMvc.perform(post("/pay").with(csrf()).session(session))
                .andExpect(redirectedUrl("/receipt"));

        BigDecimal expectedDiscount = book.getPrice().multiply(new BigDecimal("0.10"));
        var placed = orderRepository.findAllByOrderByPlacedAtDesc().get(0);
        assertThat(placed.getPromoCode()).isEqualTo("WELCOME10");
        assertThat(placed.getDiscount()).isEqualByComparingTo(expectedDiscount);
        assertThat(placed.getTotal()).isEqualByComparingTo(book.getPrice().subtract(expectedDiscount));
        assertThat(placed.getStatus()).isEqualTo(OrderStatus.PLACED);
    }

    @Test
    @Order(2)
    @WithMockUser(username = "demo@bookstore.local", roles = "CUSTOMER")
    void unknownPromoCodeIsRejected() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/cart/add/{barcode}", PROMO_BOOK).with(csrf()).session(session));
        mockMvc.perform(post("/cart/promo").param("code", "NOPE123").with(csrf()).session(session))
                .andExpect(redirectedUrl("/cart"));
        mockMvc.perform(get("/cart").session(session))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("That promo code does not exist")));
    }

    @Test
    @Order(3)
    @WithMockUser(username = "demo@bookstore.local", roles = "CUSTOMER")
    void reviewIsUpsertedAndVisibleOnDetailPage() throws Exception {
        mockMvc.perform(post("/reviews")
                        .param("bookId", REVIEW_BOOK)
                        .param("rating", "4")
                        .param("text", "Great intro to Go — concise and practical.")
                        .with(csrf()))
                .andExpect(redirectedUrl("/books/" + REVIEW_BOOK));

        assertThat(reviewService.visibleForBook(REVIEW_BOOK))
                .anyMatch(r -> r.getText().equals("Great intro to Go — concise and practical.")
                        && r.getRating() == 4
                        && r.getCustomer().getEmail().equals("demo@bookstore.local"));

        mockMvc.perform(get("/books/" + REVIEW_BOOK))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Great intro to Go")));

        // One review per customer per book: a second submit replaces the first.
        mockMvc.perform(post("/reviews")
                .param("bookId", REVIEW_BOOK).param("rating", "5").param("text", "Updated take.").with(csrf()));
        assertThat(reviewService.visibleForBook(REVIEW_BOOK))
                .filteredOn(r -> r.getCustomer().getEmail().equals("demo@bookstore.local"))
                .hasSize(1);
    }

    @Test
    @Order(4)
    @WithMockUser(username = "demo@bookstore.local", roles = "CUSTOMER")
    void wishlistToggleAndCatalogFilters() throws Exception {
        boolean added = wishlistService.toggle("demo@bookstore.local", PROMO_BOOK);
        assertThat(added).isTrue();
        assertThat(wishlistService.contains("demo@bookstore.local", PROMO_BOOK)).isTrue();

        mockMvc.perform(get("/books").param("category", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data Structures Through C")));

        mockMvc.perform(get("/books").param("stock", "in"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString(">Sold out<"))));
    }

    @Test
    @Order(5)
    @WithMockUser(username = "demo@bookstore.local", roles = "CUSTOMER")
    void customerCancelRestocksAndSellerAdvancesStatus() throws Exception {
        Book book = bookRepository.findById(PROMO_BOOK).orElseThrow();
        int before = book.getQuantity();
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/cart/add/{barcode}", PROMO_BOOK).with(csrf()).session(session));
        mockMvc.perform(post("/pay").with(csrf()).session(session));

        var order = orderRepository.findAllByOrderByPlacedAtDesc().get(0);
        assertThat(bookRepository.findById(PROMO_BOOK).orElseThrow().getQuantity()).isEqualTo(before - 1);

        mockMvc.perform(post("/orders/{id}/cancel", order.getId()).with(csrf()))
                .andExpect(redirectedUrl("/orders/" + order.getId()));
        assertThat(orderRepository.findById(order.getId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.CANCELLED);
        assertThat(bookRepository.findById(PROMO_BOOK).orElseThrow().getQuantity()).isEqualTo(before);

        // New order, advanced by the seller: PLACED -> SHIPPED -> DELIVERED.
        MockHttpSession session2 = new MockHttpSession();
        mockMvc.perform(post("/cart/add/{barcode}", PROMO_BOOK).with(csrf()).session(session2));
        mockMvc.perform(post("/pay").with(csrf()).session(session2));
        var order2 = orderRepository.findAllByOrderByPlacedAtDesc().get(0);

        mockMvc.perform(post("/admin/orders/{id}/advance", order2.getId()).with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user("admin@bookstore.local").roles("SELLER")))
                .andExpect(redirectedUrl("/admin/orders/" + order2.getId()));
        mockMvc.perform(post("/admin/orders/{id}/advance", order2.getId()).with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user("admin@bookstore.local").roles("SELLER")))
                .andExpect(redirectedUrl("/admin/orders/" + order2.getId()));
        assertThat(orderRepository.findById(order2.getId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @Order(6)
    @WithMockUser(username = "admin@bookstore.local", roles = "SELLER")
    void adminPagesRender() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Store dashboard")));
        mockMvc.perform(get("/admin/customers"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/promos"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/reviews"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/export/orders.csv"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Order ID")));
    }

    @Test
    @Order(7)
    void publicApiAndDetailPageAreOpen() throws Exception {
        mockMvc.perform(get("/api/books").param("q", "java"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Head First Java")));
        mockMvc.perform(get("/books/9780134190500"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Data Structures Through C")));
        mockMvc.perform(get("/books/9780134190500"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Reviews")));
        mockMvc.perform(get("/trending"))
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @Order(8)
    void contactFormStoresMessage() throws Exception {
        mockMvc.perform(post("/contact")
                        .param("name", "Curious Reader")
                        .param("email", "reader@example.com")
                        .param("subject", "Order question")
                        .param("message", "Where is my package?")
                        .with(csrf()))
                .andExpect(status().isOk());
        assertThat(contactService.all())
                .anyMatch(m -> m.getEmail().equals("reader@example.com") && m.getSubject().equals("Order question"));
    }
}
