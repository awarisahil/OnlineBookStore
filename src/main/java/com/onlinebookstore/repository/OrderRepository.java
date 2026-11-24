package com.onlinebookstore.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.onlinebookstore.model.OrderStatus;
import com.onlinebookstore.model.ShopOrder;

public interface OrderRepository extends JpaRepository<ShopOrder, Long> {

    List<ShopOrder> findByCustomerEmailOrderByPlacedAtDesc(String customerEmail);

    @EntityGraph(attributePaths = { "customer" })
    List<ShopOrder> findAllByOrderByPlacedAtDesc();

    @EntityGraph(attributePaths = { "customer" })
    List<ShopOrder> findByStatusOrderByPlacedAtDesc(OrderStatus status);

    @EntityGraph(attributePaths = { "items" })
    Optional<ShopOrder> findByIdAndCustomerEmail(Long id, String customerEmail);

    @EntityGraph(attributePaths = { "items", "customer" })
    Optional<ShopOrder> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = { "items", "customer" })
    List<ShopOrder> findTop5ByOrderByPlacedAtDesc();

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM ShopOrder o WHERE o.status <> com.onlinebookstore.model.OrderStatus.CANCELLED")
    BigDecimal totalRevenue();

    @Query("SELECT COUNT(o) FROM ShopOrder o WHERE o.status <> com.onlinebookstore.model.OrderStatus.CANCELLED")
    long countNonCancelled();

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM OrderItem i JOIN i.order o "
            + "WHERE o.status <> com.onlinebookstore.model.OrderStatus.CANCELLED")
    long totalCopiesSold();
}
