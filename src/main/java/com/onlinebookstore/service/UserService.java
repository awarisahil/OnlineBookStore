package com.onlinebookstore.service;

import java.util.List;

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

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findById(email).orElseThrow();
    }

    @Transactional
    public void updateProfile(String email, String firstName, String lastName, String address, String phone) {
        User user = findByEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAddress(address);
        user.setPhone(phone);
    }

    @Transactional
    public boolean changePassword(String email, String currentPassword, String newPassword) {
        User user = findByEmail(email);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            return false;
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        return true;
    }

    @Transactional(readOnly = true)
    public List<User> all() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public long count() {
        return userRepository.count();
    }

    @Transactional
    public void setRole(String email, Role role) {
        findByEmail(email).setRole(role);
    }

    @Transactional
    public void setEnabled(String email, boolean enabled) {
        findByEmail(email).setEnabled(enabled);
    }
}
