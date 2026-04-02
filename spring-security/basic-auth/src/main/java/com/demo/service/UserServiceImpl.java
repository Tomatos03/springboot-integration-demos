package com.demo.service;

import com.demo.entity.TUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
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
        log.info("[BasicAuth] DaoAuthenticationProvider 调用 loadUserByUsername 加载用户: {}", username);
        TUser user = users.get(username);
        if (user == null) {
            log.warn("[BasicAuth] 用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在");
        }
        log.info("[BasicAuth] 用户加载成功，权限: {}", user.getAuthorities());
        return user;
    }
}
