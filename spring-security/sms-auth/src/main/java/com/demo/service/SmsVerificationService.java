package com.demo.service;

import cn.hutool.core.util.RandomUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SmsVerificationService {

    private static final String CODE_PREFIX = "sms:code:";
    private static final long CODE_EXPIRE_MINUTES = 5;

    private final StringRedisTemplate redisTemplate;

    public SmsVerificationService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateCode(String phone) {
        String code = RandomUtil.randomNumbers(6);
        redisTemplate.opsForValue().set(CODE_PREFIX + phone, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        return code;
    }

    public boolean verifyCode(String phone, String code) {
        String cached = redisTemplate.opsForValue().get(CODE_PREFIX + phone);
        if (cached == null) {
            return false;
        }
        boolean matched = cached.equals(code);
        if (matched) {
            redisTemplate.delete(CODE_PREFIX + phone);
        }
        return matched;
    }
}
