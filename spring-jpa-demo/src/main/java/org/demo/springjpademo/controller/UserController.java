package org.demo.springjpademo.controller;

import lombok.RequiredArgsConstructor;
import org.demo.springjpademo.dto.UserDTO;
import org.demo.springjpademo.dto.UserResponseDTO;
import org.demo.springjpademo.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : Tomatos
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/name/{name}")
    public UserDTO getUserByName(@PathVariable String name) {
        return userService.findUserByName(name);
    }

    @GetMapping("/email/{email}")
    public UserResponseDTO getUserByEmail(@PathVariable String email) {
        return UserResponseDTO.fromEntity(userService.findUserByEmail(email));
    }

    @DeleteMapping("/email/{email}")
    public long deleteUserByEmail(@PathVariable String email) {
        return userService.deleteUserByEmail(email);
    }

    @PutMapping("/email/{email}")
    public int updateNameByEmail(@RequestParam String newName, @PathVariable String email) {
        return userService.updateNameByEmail(newName, email);
    }

    @GetMapping("/complex")
    public List<UserResponseDTO> getComplexUsers(@RequestParam String keyword, @RequestParam int minOrderCount) {
        return userService.findComplexUsers(keyword, minOrderCount).stream()
                .map(UserResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/regex")
    public List<UserResponseDTO> getUsersWithNativeRegex() {
        return userService.findUsersWithNativeRegex().stream()
                .map(UserResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/page")
    public Page<UserResponseDTO> getAllUsersPaged(Pageable pageable) {
        return userService.findAllUsers(pageable).map(UserResponseDTO::fromEntity);
    }

    @GetMapping("/all")
    public List<UserDTO> getAllUsers() {
        return userService.findAllProjectedBy();
    }
}