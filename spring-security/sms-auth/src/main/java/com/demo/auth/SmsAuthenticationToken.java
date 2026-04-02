package com.demo.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 短信认证 Token
 * 
 * 继承自 AbstractAuthenticationToken，用于封装短信认证的信息。
 * 在 Spring Security 认证流程中充当 Authentication 对象的角色。
 * 
 * 状态说明：
 * 1. 未认证状态：来自 SmsAuthenticationFilter，包含手机号和验证码
 *    - principal：手机号
 *    - credentials：验证码
 *    - authorities：空（isAuthenticated() = false）
 * 
 * 2. 已认证状态：来自 SmsAuthenticationProvider 认证成功后
 *    - principal：UserDetails 对象（用户信息）
 *    - credentials：null
 *    - authorities：用户权限列表
 *    - isAuthenticated() = true
 * 
 * 两个构造方法分别对应这两种状态。
 */
public class SmsAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final Object credentials;

    /**
     * 未认证状态构造方法
     * 
     * 用于 SmsAuthenticationFilter 中构建待认证的 Token
     * 
     * @param principal 手机号
     * @param credentials 验证码
     */
    public SmsAuthenticationToken(Object principal, Object credentials) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(false);
    }

    /**
     * 已认证状态构造方法
     * 
     * 用于 SmsAuthenticationProvider 中构建已认证的 Token
     * 
     * @param principal UserDetails 对象，包含用户信息
     * @param credentials 密码，此处为 null（验证码已验证，不再需要）
     * @param authorities 用户权限列表
     */
    public SmsAuthenticationToken(Object principal, Object credentials,
                                  Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
