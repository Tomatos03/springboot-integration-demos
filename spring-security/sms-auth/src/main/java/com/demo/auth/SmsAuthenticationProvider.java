package com.demo.auth;

import com.demo.service.SmsVerificationService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public class SmsAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final SmsVerificationService smsVerificationService;

    public SmsAuthenticationProvider(UserDetailsService userDetailsService,
                                     SmsVerificationService smsVerificationService) {
        this.userDetailsService = userDetailsService;
        this.smsVerificationService = smsVerificationService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String phone = (String) authentication.getPrincipal();
        String code = (String) authentication.getCredentials();

        if (!smsVerificationService.verifyCode(phone, code)) {
            throw new AuthenticationException("验证码错误或已过期") {};
        }

        UserDetails user = userDetailsService.loadUserByUsername(phone);
        return new SmsAuthenticationToken(user, null, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
