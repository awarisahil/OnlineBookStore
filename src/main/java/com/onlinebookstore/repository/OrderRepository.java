package com.onlinebookstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.onlinebookstore.model.ShopOrder;

public interface OrderRepository extends JpaRepository<ShopOrder, Long> {

    List<ShopOrder> findByCustomerEmailOrderByPlacedAtDesc(String customerEmail);

    @EntityGraph(attributePaths = { "customer" })
    List<ShopOrder> findAllByOrderByPlacedAtDesc();

    @EntityGraph(attributePaths = { "items" })
    Optional<ShopOrder> findByIdAndCustomerEmail(Long id, String customerEmail);

    @EntityGraph(attributePaths = { "items", "customer" })
    Optional<ShopOrder> findWithDetailsById(Long id);
}
