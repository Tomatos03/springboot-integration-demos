package org.demo.springjpademo.service;

import lombok.RequiredArgsConstructor;
import org.demo.springjpademo.entity.OrderStatus;
import org.demo.springjpademo.entity.PurchaseOrder;
import org.demo.springjpademo.entity.User;
import org.demo.springjpademo.repository.PurchaseOrderRepository;
import org.demo.springjpademo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Transactional
    public PurchaseOrder createOrder(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found, id=" + userId));

        PurchaseOrder order = PurchaseOrder.builder()
                .user(user)
                .amount(amount)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        return purchaseOrderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> findPaidOrdersGreaterThan(BigDecimal amount) {
        return purchaseOrderRepository.findByStatusAndAmountGreaterThan(OrderStatus.PAID, amount);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrder> pageByStatus(OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return purchaseOrderRepository.findByStatus(status, pageable);
    }

    @Transactional
    public PurchaseOrder payOrder(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found, id=" + orderId));
        order.setStatus(OrderStatus.PAID);
        return order;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> findByUserEmail(String email) {
        return purchaseOrderRepository.findAllByUserEmail(email);
    }
}