package com.onlinebookstore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onlinebookstore.model.ContactMessage;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    List<ContactMessage> findAllByOrderByCreatedAtDesc();

    long countByReadFlagFalse();
}
