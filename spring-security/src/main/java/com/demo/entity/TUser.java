package com.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Permission;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author : Tomatos
 * @date : 2025/7/29
 */
@Data
public class TUser implements UserDetails {
    private Integer id;
    private String loginAct;
    @JsonIgnore
    private String loginPwd;
    private String name;
    private String phone;
    private String email;
    private Integer accountNoExpired;
    private Integer credentialsNoExpired;
    private Integer accountNoLocked;
    private Integer accountEnabled;
    private LocalDateTime createTime;
    private Integer createBy;
    private LocalDateTime editTime;
    private Integer editBy;
    private LocalDateTime lastLoginTime;
    @JsonIgnore
    private List<Permission> permissions;
    @JsonIgnore
    private List<TRole> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        ArrayList<GrantedAuthority> list = new ArrayList<>();

        // 基于角色配置
        final String ROLE_PREFIX = "ROLE_"; // 必须加上ROLE_前缀, 用于区分基于权限码的配置
        roles.forEach(role -> {
            list.add(new SimpleGrantedAuthority(ROLE_PREFIX + role.getRole()));
        });
        return list;
    }

    @Override
    public String getPassword() {
        return loginPwd;
    }


    @Override
    public String getUsername() {
        return loginAct;
    }
}
