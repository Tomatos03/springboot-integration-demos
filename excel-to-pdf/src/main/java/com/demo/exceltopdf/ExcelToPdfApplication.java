package com.demo.exceltopdf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * excel-to-pdf：复用 fesode-excel（POI）生成多 Sheet Excel，再经 LibreOffice headless
 * 转换为每 Sheet 一页的 PDF。
 *
 * <p>{@code @ConfigurationPropertiesScan} 用于注册 {@code config} 包下的
 * {@code LibreOfficeProperties} 配置 Bean。
 *
 * @author Tomatos
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ExcelToPdfApplication {

    /**
     * 应用入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ExcelToPdfApplication.class, args);
    }
}
