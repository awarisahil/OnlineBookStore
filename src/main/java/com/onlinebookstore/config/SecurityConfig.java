package com.onlinebookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.onlinebookstore.model.User;
import com.onlinebookstore.repository.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/about", "/faq", "/contact", "/login", "/register",
                                "/error/**", "/css/**", "/webjars/**")
                        .permitAll()
                        .requestMatchers("/books/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("SELLER")
                        .requestMatchers("/books", "/trending", "/new-arrivals", "/cart/**", "/checkout",
                                "/pay", "/receipt", "/orders/**", "/wishlist/**", "/reviews/**",
                                "/notify/**", "/profile/**")
                        .hasRole("CUSTOMER")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(new RoleBasedAuthenticationSuccessHandler())
                        .failureUrl("/login?error")
                        .permitAll())
                .rememberMe(remember -> remember
                        .key("onlinebookstore-remember-me")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                        .alwaysRemember(false))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .exceptionHandling(handling -> handling.accessDeniedPage("/error/403"));
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return email -> userRepository.findById(email)
                .map(SecurityConfig::toUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("No account for " + email));
    }

    private static UserDetails toUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .disabled(!user.isEnabled())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
