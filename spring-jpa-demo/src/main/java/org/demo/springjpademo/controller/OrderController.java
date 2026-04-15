package org.demo.springjpademo.controller;

import org.demo.springjpademo.entity.OrderStatus;
import org.demo.springjpademo.entity.PurchaseOrder;
import org.demo.springjpademo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public PurchaseOrder createOrder(@RequestParam Long userId, @RequestParam BigDecimal amount) {
        return orderService.createOrder(userId, amount);
    }

    @PutMapping("/{id}/pay")
    public PurchaseOrder payOrder(@PathVariable Long id) {
        return orderService.payOrder(id);
    }

    @GetMapping("/paid-over")
    public List<PurchaseOrder> findPaidOrdersOver(@RequestParam BigDecimal amount) {
        return orderService.findPaidOrdersGreaterThan(amount);
    }

    @GetMapping
    public Page<PurchaseOrder> pageByStatus(@RequestParam OrderStatus status,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return orderService.pageByStatus(status, page, size);
    }

    @GetMapping("/by-user-email")
    public List<PurchaseOrder> listByUserEmail(@RequestParam String email) {
        return orderService.findByUserEmail(email);
    }
}