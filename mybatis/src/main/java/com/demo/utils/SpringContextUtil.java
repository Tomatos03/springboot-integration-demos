package com.demo.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author : Tomatos
 * @date : 2025/7/11
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {
    private static ApplicationContext springContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        springContext = applicationContext;
    }

    public static <T> T getBean(Class<T> classz) {
        return springContext.getBean(classz);
    }
}
