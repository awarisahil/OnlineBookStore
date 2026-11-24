package com.onlinebookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onlinebookstore.model.PromoCode;

public interface PromoCodeRepository extends JpaRepository<PromoCode, String> {
}
