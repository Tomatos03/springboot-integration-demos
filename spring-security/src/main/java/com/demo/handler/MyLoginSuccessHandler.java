package com.demo.handler;

import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWTUtil;
import com.demo.constant.RedisConst;
import com.demo.entity.TUser;
import com.demo.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author : Tomatos
 * @date : 2025/7/30
 */
@Slf4j
@Component
public class MyLoginSuccessHandler implements AuthenticationSuccessHandler {
    @Value("${my-mall.jwt.secret}")
    private String secret;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        TUser user = (TUser) authentication.getPrincipal();
        user.setLoginPwd(null);

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("user", user);
        String token = JWTUtil.createToken(payload, secret.getBytes());

        Result<String> result = Result.success("登录成功", token);
        String resJson = JSONUtil.toJsonStr(result);

//        String tokenKey = RedisConst.TOKEN_KEY + token;
        String tokenKey = RedisConst.TOKEN_KEY + user.getId();
        stringRedisTemplate.opsForValue().set(tokenKey,
                                              token,
                                              1,
                                              TimeUnit.DAYS);

        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(resJson);
    }
}
