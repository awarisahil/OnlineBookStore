package com.onlinebookstore.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onlinebookstore.dto.Receipt;
import com.onlinebookstore.dto.ReceiptLine;
import com.onlinebookstore.model.CartLine;
import com.onlinebookstore.model.OrderItem;
import com.onlinebookstore.model.OrderStatus;
import com.onlinebookstore.model.ShopOrder;
import com.onlinebookstore.model.User;
import com.onlinebookstore.repository.OrderRepository;
import com.onlinebookstore.repository.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookService bookService;
    private final PromoService promoService;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, BookService bookService,
            PromoService promoService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.bookService = bookService;
        this.promoService = promoService;
    }

    /**
     * Purchase every cart line (atomic stock decrement) and persist the order in
     * the same transaction, applying the promo code discount. Throws
     * OutOfStockException and rolls everything back if any line cannot be
     * fulfilled.
     */
    @Transactional
    public Receipt placeOrder(String customerEmail, List<CartLine> lines, String promoCode) {
        User customer = userRepository.findById(customerEmail)
                .orElseThrow(() -> new AccessDeniedException("Unknown customer"));

        ShopOrder order = new ShopOrder();
        order.setCustomer(customer);
        order.setPlacedAt(LocalDateTime.now());
        order.setShippingAddress(customer.getFullName() + ", " + customer.getAddress());

        Receipt receipt = new Receipt();
        receipt.setPlacedAt(order.getPlacedAt());
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartLine line : lines) {
            bookService.purchaseLine(line);
            order.addItem(new OrderItem(line.getBarcode(), line.getName(), line.getPrice(), line.getQuantity()));
            receipt.getOrInitLines().add(new ReceiptLine(line.getBarcode(), line.getName(), null,
                    line.getPrice(), line.getQuantity()));
            subtotal = subtotal.add(line.getAmount());
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (promoCode != null && !promoCode.isBlank()) {
            var result = promoService.validate(promoCode, subtotal);
            if (result.applied()) {
                discount = result.discount();
                order.setPromoCode(result.promo().getCode());
                promoService.consume(result.promo().getCode());
            }
        }
        order.setDiscount(discount);
        order.setTotal(subtotal.subtract(discount));
        orderRepository.save(order);

        receipt.setOrderId(order.getId());
        receipt.setSubtotal(subtotal);
        receipt.setDiscount(discount);
        receipt.setPromoCode(order.getPromoCode());
        receipt.setTotal(order.getTotal());
        return receipt;
    }

    @Transactional(readOnly = true)
    public List<ShopOrder> findForCustomer(String customerEmail) {
        return orderRepository.findByCustomerEmailOrderByPlacedAtDesc(customerEmail);
    }

    @Transactional(readOnly = true)
    public ShopOrder findForCustomer(Long orderId, String customerEmail) {
        return orderRepository.findByIdAndCustomerEmail(orderId, customerEmail)
                .orElseThrow(() -> new AccessDeniedException("Not your order"));
    }

    @Transactional(readOnly = true)
    public List<ShopOrder> findAll() {
        return orderRepository.findAllByOrderByPlacedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<ShopOrder> findByStatus(OrderStatus status) {
        return status == null ? findAll() : orderRepository.findByStatusOrderByPlacedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public ShopOrder findById(Long orderId) {
        return orderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new AccessDeniedException("Unknown order"));
    }

    /** Customer-initiated cancellation: only while PLACED; stock is returned. */
    @Transactional
    public void cancelByCustomer(Long orderId, String customerEmail, String reason) {
        ShopOrder order = findForCustomer(orderId, customerEmail);
        cancel(order, reason);
    }

    /** Seller-initiated cancellation with a reason; stock is returned. */
    @Transactional
    public void cancelBySeller(Long orderId, String reason) {
        cancel(findById(orderId), reason);
    }

    private void cancel(ShopOrder order, String reason) {
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new IllegalStateException("Only placed orders can be cancelled.");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason == null || reason.isBlank() ? "Cancelled" : reason.trim());
        for (OrderItem item : order.getItems()) {
            bookService.restock(item.getBarcode(), item.getQuantity());
        }
    }

    /** Advance PLACED -> SHIPPED -> DELIVERED. */
    @Transactional
    public void advance(Long orderId) {
        ShopOrder order = findById(orderId);
        switch (order.getStatus()) {
            case PLACED -> order.setStatus(OrderStatus.SHIPPED);
            case SHIPPED -> order.setStatus(OrderStatus.DELIVERED);
            default -> throw new IllegalStateException("Order " + orderId + " is already " + order.getStatus());
        }
    }

    @Transactional(readOnly = true)
    public List<ShopOrder> recent() {
        return orderRepository.findTop5ByOrderByPlacedAtDesc();
    }

    @Transactional(readOnly = true)
    public BigDecimal totalRevenue() {
        return orderRepository.totalRevenue();
    }

    @Transactional(readOnly = true)
    public long nonCancelledCount() {
        return orderRepository.countNonCancelled();
    }

    @Transactional(readOnly = true)
    public long copiesSold() {
        return orderRepository.totalCopiesSold();
    }
}
