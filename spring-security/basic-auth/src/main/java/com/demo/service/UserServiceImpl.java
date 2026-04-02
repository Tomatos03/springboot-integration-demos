package com.demo.service;

import com.demo.entity.TUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserServiceImpl implements UserDetailsService {

    private final Map<String, TUser> users = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl() {
        TUser user = new TUser();
        user.setLoginPwd(passwordEncoder.encode("123456"));
        user.setAuthorities(Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        user.setName("管理员");
        users.put("admin", user);
    }

    @Override
    public TUser loadUserByUsername(String username) throws UsernameNotFoundException {
        TUser user = users.get(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return user;
    }
}
