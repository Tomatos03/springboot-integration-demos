package com.demo.service.impl;

import com.demo.entity.TUser;
import com.demo.entity.TRole;
import com.demo.mapper.TUserMapper;
import com.demo.mapper.RoleMapper;
import com.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author : Tomatos
 * @date : 2025/7/29
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    TUserMapper userMapper;
    @Autowired
    RoleMapper roleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TUser user = userMapper.queryByUsername(username);
        if (user == null)
            throw new UsernameNotFoundException("用户不存在");

        List<TRole> tRoles = roleMapper.queryRolesByUserId(user.getId());
        user.setRoles(tRoles);
        return user;
    }
}
