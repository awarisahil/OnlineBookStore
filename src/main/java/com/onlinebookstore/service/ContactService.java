package com.onlinebookstore.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onlinebookstore.model.ContactMessage;
import com.onlinebookstore.repository.ContactMessageRepository;

@Service
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;

    public ContactService(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @Transactional
    public ContactMessage submit(String name, String email, String subject, String message) {
        return contactMessageRepository.save(new ContactMessage(name, email, subject, message));
    }

    @Transactional(readOnly = true)
    public List<ContactMessage> all() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        return contactMessageRepository.countByReadFlagFalse();
    }

    @Transactional
    public void markRead(Long id) {
        contactMessageRepository.findById(id).ifPresent(m -> m.setReadFlag(true));
    }

    @Transactional
    public void delete(Long id) {
        contactMessageRepository.deleteById(id);
    }
}
