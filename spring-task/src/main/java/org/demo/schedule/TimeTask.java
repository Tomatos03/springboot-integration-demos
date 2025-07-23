package org.demo.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author : Tomatos
 * @date : 2025/7/23
 */
@Component
public class TimeTask {
    @Scheduled(fixedRate = 3000) // 单位毫秒
    public void printTime() {
        System.out.println(System.currentTimeMillis());
    }
}
