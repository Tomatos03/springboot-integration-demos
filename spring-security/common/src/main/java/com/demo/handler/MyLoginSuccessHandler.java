package com.demo.handler;

import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWTUtil;
import com.demo.entity.TUser;
import com.demo.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.HashMap;

public class MyLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${jwt.secret:defaultSecretKey123456}")
    private String secret;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        TUser user = (TUser) authentication.getPrincipal();

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("user", user);
        String token = JWTUtil.createToken(payload, secret.getBytes());

        Result<String> result = Result.success("登录成功", token);
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(result));
    }
}