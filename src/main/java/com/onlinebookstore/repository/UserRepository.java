package com.onlinebookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onlinebookstore.model.User;

public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByEmail(String email);
}
