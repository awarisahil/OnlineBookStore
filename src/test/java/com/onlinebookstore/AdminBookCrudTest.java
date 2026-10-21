package com.onlinebookstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.onlinebookstore.repository.BookRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdminBookCrudTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    BookRepository bookRepository;

    @Test
    @WithMockUser(roles = "SELLER")
    void inventoryListsBooks() throws Exception {
        mockMvc.perform(get("/admin/books"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Book Inventory")))
                .andExpect(content().string(containsString("Data Structures Through C")));
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void createGeneratesUuidBarcode() throws Exception {
        mockMvc.perform(post("/admin/books")
                        .param("name", "Test Book")
                        .param("author", "Test Author")
                        .param("price", "12.50")
                        .param("quantity", "7")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/books?created"));

        var created = bookRepository.findAll().stream()
                .filter(b -> b.getName().equals("Test Book"))
                .findFirst().orElseThrow();
        assertThat(created.getBarcode()).matches("[0-9a-f-]{36}");
        assertThat(created.getPrice()).isEqualByComparingTo("12.50");
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void invalidBookIsRejectedWithErrors() throws Exception {
        mockMvc.perform(post("/admin/books")
                        .param("name", "")
                        .param("author", "")
                        .param("price", "-3")
                        .param("quantity", "-1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Price must be zero or greater")));
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void updateAndDeleteWork() throws Exception {
        // create a dedicated book so other tests are unaffected by its mutation
        mockMvc.perform(post("/admin/books")
                        .param("name", "Transient Book")
                        .param("author", "Temp Author")
                        .param("price", "10.00")
                        .param("quantity", "2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
        String barcode = bookRepository.findAll().stream()
                .filter(b -> b.getName().equals("Transient Book"))
                .findFirst().orElseThrow()
                .getBarcode();

        mockMvc.perform(post("/admin/books/{barcode}", barcode)
                        .param("name", "Transient Book")
                        .param("author", "Temp Author")
                        .param("price", "15.75")
                        .param("quantity", "9")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/books?updated"));
        assertThat(bookRepository.findById(barcode).orElseThrow().getPrice())
                .isEqualByComparingTo("15.75");

        mockMvc.perform(post("/admin/books/{barcode}/delete", barcode).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/books?deleted"));
        assertThat(bookRepository.existsById(barcode)).isFalse();
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerIsForbiddenFromAdminArea() throws Exception {
        mockMvc.perform(get("/admin/books")).andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/books").param("name", "X").param("author", "Y")
                .param("price", "1").param("quantity", "1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void deleteDoesNotAcceptGet() throws Exception {
        mockMvc.perform(get("/admin/books/{barcode}/delete", "9780134190500"))
                .andExpect(status().isMethodNotAllowed());
    }
}
