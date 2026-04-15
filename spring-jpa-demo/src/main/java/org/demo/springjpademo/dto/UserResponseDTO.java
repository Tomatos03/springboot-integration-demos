package org.demo.springjpademo.dto;

import org.demo.springjpademo.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public record UserResponseDTO(
        Long id,
        String email,
        String name,
        List<OrderResponseDTO> orders
) {
    public static UserResponseDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }
        List<OrderResponseDTO> orderDTOs = user.getOrders() == null ? null : user.getOrders().stream()
                .map(OrderResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return new UserResponseDTO(user.getId(), user.getEmail(), user.getName(), orderDTOs);
    }
}