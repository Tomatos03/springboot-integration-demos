package com.demo.auth;

import com.demo.service.SmsVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 短信认证 Provider
 * 
 * 职责：
 * 1. 实现 AuthenticationProvider 接口
 * 2. 声明支持 SmsAuthenticationToken 类型
 * 3. 验证短信验证码有效性
 * 4. 加载用户信息
 * 5. 返回已认证的 Token
 * 
 * 工作流程：
 * 1. SmsAuthenticationFilter 构建未认证的 SmsAuthenticationToken（principal=手机号, credentials=验证码）
 * 2. AuthenticationManager 轮询所有 Provider，调用 supports() 方法检查是否支持该 Token
 * 3. SmsAuthenticationProvider.supports() 返回 true（支持 SmsAuthenticationToken）
 * 4. AuthenticationManager 调用 SmsAuthenticationProvider.authenticate()
 * 5. Provider 验证验证码，加载用户信息，返回已认证的 Token
 * 6. 若验证失败，抛出 AuthenticationException
 * 
 * 与表单认证 Provider（DaoAuthenticationProvider）的区别：
 * - 表单认证：验证密码（使用 PasswordEncoder 比对密码哈希）
 * - 短信认证：验证验证码（从 Redis 获取缓存的验证码进行比对）
 */
@Slf4j
public class SmsAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final SmsVerificationService smsVerificationService;

    public SmsAuthenticationProvider(UserDetailsService userDetailsService,
                                     SmsVerificationService smsVerificationService) {
        this.userDetailsService = userDetailsService;
        this.smsVerificationService = smsVerificationService;
    }

    /**
     * 认证逻辑
     * 
     * @param authentication 未认证的 SmsAuthenticationToken，包含手机号和验证码
     * @return 已认证的 SmsAuthenticationToken，包含用户信息和权限
     * @throws AuthenticationException 验证码错误或用户不存在时抛出
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("[SmsAuthenticationProvider] 开始认证流程");
        
        // 1. 从 Token 中提取手机号和验证码
        String phone = (String) authentication.getPrincipal();
        String code = (String) authentication.getCredentials();
        
        log.info("[SmsAuthenticationProvider] 提取参数: phone={}", phone);

        // 2. 验证验证码有效性
        // 调用 SmsVerificationService 从 Redis 获取缓存的验证码进行比对
        // 若验证码不存在、已过期或不匹配，返回 false
        log.info("[SmsAuthenticationProvider] 开始验证验证码");
        if (!smsVerificationService.verifyCode(phone, code)) {
            log.warn("[SmsAuthenticationProvider] 验证码验证失败: phone={}", phone);
            // 验证码错误或已过期，抛出 AuthenticationException
            throw new AuthenticationException("验证码错误或已过期") {};
        }

        log.info("[SmsAuthenticationProvider] 验证码验证成功");

        // 3. 验证码验证通过，加载用户信息
        // 调用 UserDetailsService 根据手机号从数据库加载用户信息
        log.info("[SmsAuthenticationProvider] 从数据库加载用户信息: phone={}", phone);
        UserDetails user = userDetailsService.loadUserByUsername(phone);
        
        log.info("[SmsAuthenticationProvider] 用户加载成功，构建已认证Token");
        
        // 4. 构建已认证的 SmsAuthenticationToken
        // 参数：UserDetails 对象，null，用户权限列表
        // isAuthenticated() = true（因为包含 authorities）
        return new SmsAuthenticationToken(user, null, user.getAuthorities());
    }

    /**
     * 声明本 Provider 支持的 Token 类型
     * 
     * AuthenticationManager 轮询所有 Provider 时，会调用此方法检查
     * 本 Provider 是否支持当前 Token
     * 
     * @param authentication Token 类型（通常是 SmsAuthenticationToken）
     * @return true 表示支持本 Token 类型，false 表示不支持
     */
    @Override
    public boolean supports(Class<?> authentication) {
        // 返回 true 表示本 Provider 支持 SmsAuthenticationToken 及其子类
        return SmsAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
