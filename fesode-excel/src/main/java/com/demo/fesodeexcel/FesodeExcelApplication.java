package com.demo.fesodeexcel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * fesode-excel：基于 POI + Apache Fesod 的单 Sheet 模板动态多 Sheet Excel 演示。
 *
 * @author fesode
 */
@SpringBootApplication
public class FesodeExcelApplication {

    /**
     * 应用入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(FesodeExcelApplication.class, args);
    }
}
