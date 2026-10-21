package com.onlinebookstore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.onlinebookstore.model.Role;
import com.onlinebookstore.model.User;
import com.onlinebookstore.repository.UserRepository;

/**
 * Seeds the two demo accounts on first start, hashing passwords with the
 * application's encoder. Books are seeded by Flyway (V2).
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        userRepository.save(new User("admin@bookstore.local", passwordEncoder.encode("admin123"),
                "Store", "Admin", "Bookstore HQ", "1000000000", Role.SELLER));
        userRepository.save(new User("demo@bookstore.local", passwordEncoder.encode("demo123"),
                "Demo", "User", "123 Reader Lane", "2000000000", Role.CUSTOMER));
        log.info("Seeded demo accounts: admin@bookstore.local (seller), demo@bookstore.local (customer)");
    }
}
