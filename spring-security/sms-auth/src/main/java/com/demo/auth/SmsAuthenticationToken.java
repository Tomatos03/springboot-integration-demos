package com.demo.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class SmsAuthenticationToken extends UsernamePasswordAuthenticationToken {

    public SmsAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public SmsAuthenticationToken(Object principal, Object credentials,
                                  Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }
}
