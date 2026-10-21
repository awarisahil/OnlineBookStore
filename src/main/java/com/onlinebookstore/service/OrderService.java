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
import com.onlinebookstore.model.ShopOrder;
import com.onlinebookstore.model.User;
import com.onlinebookstore.repository.OrderRepository;
import com.onlinebookstore.repository.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookService bookService;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, BookService bookService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.bookService = bookService;
    }

    /**
     * Purchase every cart line (atomic stock decrement) and persist the order in
     * the same transaction. Throws OutOfStockException and rolls everything back
     * if any line cannot be fulfilled.
     */
    @Transactional
    public Receipt placeOrder(String customerEmail, List<CartLine> lines) {
        User customer = userRepository.findById(customerEmail)
                .orElseThrow(() -> new AccessDeniedException("Unknown customer"));

        ShopOrder order = new ShopOrder();
        order.setCustomer(customer);
        order.setPlacedAt(LocalDateTime.now());

        Receipt receipt = new Receipt();
        receipt.setPlacedAt(order.getPlacedAt());
        BigDecimal total = BigDecimal.ZERO;

        for (CartLine line : lines) {
            bookService.purchaseLine(line);
            order.addItem(new OrderItem(line.getBarcode(), line.getName(), line.getPrice(), line.getQuantity()));
            receipt.getOrInitLines().add(new ReceiptLine(line.getBarcode(), line.getName(), null,
                    line.getPrice(), line.getQuantity()));
            total = total.add(line.getAmount());
        }

        order.setTotal(total);
        orderRepository.save(order);

        receipt.setOrderId(order.getId());
        receipt.setTotal(total);
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
    public ShopOrder findById(Long orderId) {
        return orderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new AccessDeniedException("Unknown order"));
    }
}
