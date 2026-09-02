package com.demo.exceltopdf.config;

import com.demo.exceltopdf.helper.PdfHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 提供携带 application.yml 配置的 {@link PdfHelper} bean：把
 * {@code excel-to-pdf.libreoffice.*} 绑定的 {@link LibreOfficeProperties}
 * 经构造参数注入 PdfHelper 门面——注入并使用该 bean 的每一条链都以 yml 配置
 * （soffice 定位、超时、纸张大小等）执行终端转换。
 *
 * <p>脱离 Spring 的场景不需要本配置类：直接
 * {@code new PdfHelper(new LibreOfficeProperties())} 即可，内置默认值
 * （自动探测 soffice、超时 120 秒、纸张 A4）与 application.yml 默认一致。
 *
 * @author Tomatos
 */
@Configuration(proxyBeanMethods = false)
public class PdfHelperConfiguration {

    /**
     * PdfHelper 门面 bean：转换配置参数来自 application.yml 的
     * {@code excel-to-pdf.libreoffice.*}。
     *
     * @param properties LibreOffice 转换配置
     * @return 使用该配置创建链的门面实例
     */
    @Bean
    public PdfHelper pdfHelper(LibreOfficeProperties properties) {
        return new PdfHelper(properties);
    }
}
