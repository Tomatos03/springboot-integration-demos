package com.demo.filter;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTUtil;
import com.demo.constant.RedisConst;
import com.demo.entity.TUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author : Tomatos
 * @date : 2025/7/30
 */
@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Value("${my-mall.jwt.secret}")
    String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/login")) {
            filterChain.doFilter(request, response);
            return;
        }
        String userToken = request.getHeader("token");
        if (StrUtil.isEmpty(userToken)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }

        boolean verify = false;
        try {
            verify = JWTUtil.verify(userToken, secret.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!verify) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            log.info("token验证失败");
            return;
        }

        Map userObj = (Map) JWTUtil.parseToken(userToken)
                                   .getPayload("user");
        
        TUser user = BeanUtil.fillBeanWithMap(userObj, new TUser(), false);
        String tokenKey = RedisConst.TOKEN_KEY + user.getId();
        String token = stringRedisTemplate.opsForValue()
                                          .get(tokenKey);
        if (token == null) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }

        List<SimpleGrantedAuthority> roles = user.getRoles()
                                                 .stream()
                                                 .map((tRole) -> new SimpleGrantedAuthority(tRole.getRole()))
                                                 .toList();
        // 关闭Session机制之后, 每一次请求都需要重新设置Authentication对象(请求完成之后被对象被销毁)
        SecurityContextHolder.getContext()
                             .setAuthentication(new UsernamePasswordAuthenticationToken(user,
                                                                                        null,
                                                                                        roles));
        filterChain.doFilter(request, response);
    }
}
