package com.onlinebookstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.onlinebookstore.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RegistrationFlowTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Test
    void registrationFormRenders() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(content().string(containsString("Create your account")));
    }

    @Test
    void termsMustBeAccepted() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "terms@example.com")
                        .param("password", "secret1")
                        .param("firstName", "T")
                        .param("lastName", "C")
                        .param("address", "Somewhere")
                        .param("phone", "123456789")
                        .param("acceptTerms", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("terms and conditions")));
        assertThat(userRepository.existsByEmail("terms@example.com")).isFalse();
    }

    @Test
    void phoneMustBeNumeric() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "phone@example.com")
                        .param("password", "secret1")
                        .param("firstName", "P")
                        .param("lastName", "H")
                        .param("address", "Somewhere")
                        .param("phone", "abc")
                        .param("acceptTerms", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Phone must be 6 to 20 digits")));
    }

    @Test
    void validRegistrationPersistsUserAndRedirectsToLogin() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "valid@example.com")
                        .param("password", "secret1")
                        .param("firstName", "Val")
                        .param("lastName", "Id")
                        .param("address", "Somewhere 1")
                        .param("phone", "123456789")
                        .param("acceptTerms", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));
        assertThat(userRepository.existsByEmail("valid@example.com")).isTrue();
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "demo@bookstore.local")
                        .param("password", "secret1")
                        .param("firstName", "Demo")
                        .param("lastName", "Again")
                        .param("address", "Somewhere 1")
                        .param("phone", "123456789")
                        .param("acceptTerms", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("already exists")));
    }
}
