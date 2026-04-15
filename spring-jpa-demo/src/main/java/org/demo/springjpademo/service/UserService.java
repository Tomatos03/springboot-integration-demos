package org.demo.springjpademo.service;

import lombok.RequiredArgsConstructor;
import org.demo.springjpademo.dto.UserDTO;
import org.demo.springjpademo.entity.User;
import org.demo.springjpademo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/4/15
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDTO findUserByName(String name) {
        return userRepository.findByName(name);
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                             .orElse(null);
    }

    @Transactional
    public long deleteUserByEmail(String email) {
        return userRepository.deleteByEmail(email);
    }

    @Transactional
    public int updateNameByEmail(String newName, String email) {
        return userRepository.updateNameByEmail(newName, email);
    }

    public List<User> findComplexUsers(String keyword, int minOrderCount) {
        return userRepository.findComplexUsers(keyword, minOrderCount);
    }

    public List<User> findUsersWithNativeRegex() {
        return userRepository.findUsersWithNativeRegex();
    }

    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public List<UserDTO> findAllProjectedBy() {
        return userRepository.findAllProjectedBy();
    }
}