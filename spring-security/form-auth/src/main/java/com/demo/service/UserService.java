package com.demo.service;

import com.demo.entity.TUser;

public interface UserService {
    TUser loadUserByUsername(String username);
}