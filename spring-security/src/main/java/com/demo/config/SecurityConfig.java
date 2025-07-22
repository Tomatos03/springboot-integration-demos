package com.demo.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author : Tomatos
 * @date : 2025/7/13
 */
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("root")
                               .password("{noop}ppp")  // 明文密码，使用 {noop} 前缀明确告知 Spring
                               .roles("admin")
                               .build();
        return new InMemoryUserDetailsManager(user);
    }


    @Bean
    public SecurityFilterChain config(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authorization) -> {
            // 授权任何已经通过验证的请求
            authorization.anyRequest().authenticated();
        })
        .rememberMe((rememberMeConfigurer) -> {
            /*
             设置加密remember-me时的key
             用户鉴权通过后会得到加密过的remember-me, 并存储在Cookie中
             再次登录时只需要带上remember-me进行鉴权
             Spring Security 默认采用的是Session Cookie的方式, 重启服务器之后之前产生的Cookie都会失效
            */
            rememberMeConfigurer.key("token");
        })
        // 配置登录方式为表单登录, 至少配置一种登录方式
        // 表单登录发送请求的时候请求类型必须为POST
//        .formLogin(Customizer.withDefaults()) // 使用默认配置
        .formLogin((form) ->
                   form.loginPage("/login.html")
                       // 指定处理的登录表单的地址
                       .loginProcessingUrl("/doLogin")
                       // 表单验证通过之后跳转的地址
                       .defaultSuccessUrl("/demo/index")
                       // 表单验证失败后跳转的地址
                       .failureUrl("/login.html")
                       // 映射用户名和密码为表单的username参数和password参数
                       .usernameParameter("username")
                       .passwordParameter("password")
                       .permitAll()
        )
        // 关闭 csrf 跨站请求伪造保护
        .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
