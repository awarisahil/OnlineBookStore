package com.onlinebookstore;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogSearchTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void catalogListsSeededBooksWithStockBadges() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books"))
                .andExpect(content().string(containsString("Data Structures Through C")))
                .andExpect(content().string(containsString("Trending")))
                .andExpect(content().string(containsString("Only 8 left")));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void searchByTitle() throws Exception {
        mockMvc.perform(get("/books").param("q", "java"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Head First Java")))
                .andExpect(content().string(containsString("Effective Java")))
                .andExpect(content().string(not(containsString("Fluent Python"))));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void searchByAuthorIsCaseInsensitive() throws Exception {
        mockMvc.perform(get("/books").param("q", "rAmAlHo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fluent Python")))
                .andExpect(content().string(not(containsString("Head First Java"))));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void emptySearchResultShowsNote() throws Exception {
        mockMvc.perform(get("/books").param("q", "zzzznothing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No books match your search")));
    }

    @Test
    void catalogRequiresLogin() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().is3xxRedirection());
    }
}
