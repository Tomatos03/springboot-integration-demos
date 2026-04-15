package org.demo.springjpademo.config;

import org.demo.springjpademo.entity.OrderStatus;
import org.demo.springjpademo.entity.PurchaseOrder;
import org.demo.springjpademo.entity.User;
import org.demo.springjpademo.repository.PurchaseOrderRepository;
import org.demo.springjpademo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PurchaseOrderRepository orderRepository;

    public DataInitializer(UserRepository userRepository, PurchaseOrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0 || orderRepository.count() > 0) {
            return;
        }

        User tom = userRepository.save(
                User.builder()
                        .name("Tom")
                        .email("tom@example.com")
                        .build()
        );

        User jerry = userRepository.save(
                User.builder()
                        .name("Jerry")
                        .email("jerry@example.com")
                        .build()
        );

        orderRepository.save(
                PurchaseOrder.builder()
                        .user(tom)
                        .amount(new BigDecimal("99.99"))
                        .status(OrderStatus.CREATED)
                        .createdAt(LocalDateTime.now().minusDays(1))
                        .build()
        );

        orderRepository.save(
                PurchaseOrder.builder()
                        .user(tom)
                        .amount(new BigDecimal("199.00"))
                        .status(OrderStatus.PAID)
                        .createdAt(LocalDateTime.now().minusHours(5))
                        .build()
        );

        orderRepository.save(
                PurchaseOrder.builder()
                        .user(jerry)
                        .amount(new BigDecimal("29.90"))
                        .status(OrderStatus.PAID)
                        .createdAt(LocalDateTime.now().minusHours(2))
                        .build()
        );
    }
}