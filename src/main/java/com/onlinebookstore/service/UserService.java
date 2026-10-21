package com.onlinebookstore.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onlinebookstore.dto.RegistrationForm;
import com.onlinebookstore.exception.DuplicateEmailException;
import com.onlinebookstore.model.Role;
import com.onlinebookstore.model.User;
import com.onlinebookstore.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegistrationForm form) {
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new DuplicateEmailException(form.getEmail());
        }
        User user = new User(
                form.getEmail(),
                passwordEncoder.encode(form.getPassword()),
                form.getFirstName(),
                form.getLastName(),
                form.getAddress(),
                form.getPhone(),
                Role.CUSTOMER);
        return userRepository.save(user);
    }
}
