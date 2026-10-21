package com.onlinebookstore;

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
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAndErrorTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void publicPagesAreAccessible() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/about")).andExpect(status().isOk());
        mockMvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Log in")));
    }

    @Test
    void protectedPagesRedirectToLogin() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void unknownPageReturnsReal404() throws Exception {
        mockMvc.perform(get("/nope"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("404")));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void sellerAreaIsForbiddenForCustomers() throws Exception {
        mockMvc.perform(get("/admin/books")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void customerAreaIsForbiddenForSellers() throws Exception {
        mockMvc.perform(get("/cart")).andExpect(status().isForbidden());
        mockMvc.perform(get("/orders")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void logoutRequiresPostAndInvalidatesSession() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }
}
