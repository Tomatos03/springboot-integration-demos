package com.demo.handler;


import cn.hutool.json.JSONUtil;
import com.demo.constant.RedisConst;
import com.demo.entity.TUser;
import com.demo.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author : Tomatos
 * @date : 2025/7/30
 */
@Component
public class MyLogOutSuccessHandler implements LogoutSuccessHandler {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        TUser user = (TUser) authentication.getPrincipal();
        String tokenKey = RedisConst.TOKEN_KEY + user.getId();
        stringRedisTemplate.delete(tokenKey);

        Result<String> res = Result.success("登出成功");
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(res));
    }
}
