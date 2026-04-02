package com.demo.service.impl;

import com.demo.entity.TUser;
import com.demo.service.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserServiceImpl implements UserService {

    private final Map<String, TUser> users = new ConcurrentHashMap<>();

    public UserServiceImpl() {
        TUser user = new TUser();
        user.setLoginAct("13800138000");
        user.setLoginPwd("$2a$10$123456789012345678901234567890123456789012345678901234567890");
        user.setName("测试用户");
        users.put("13800138000", user);
    }

    @Override
    public TUser loadUserByUsername(String username) {
        TUser user = users.get(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return user;
    }
}