package com.demo;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 手动调用内置api
 *
 * @author : Tomatos
 * @date : 2025/7/10
 */
public class MBGGenerator {
    public static void main(String[] args) throws XMLParserException, IOException, SQLException, InterruptedException, InvalidConfigurationException {
        List<String> warnings = new ArrayList<>();
        // 如果之前生成的文件仍然存在, 会覆盖之前的文件
        boolean overwrite = true;

        // 读取 MBG 配置文件
        InputStream in = MBGGenerator.class.getResourceAsStream("/generatorConfig.xml");

        ConfigurationParser configParser = new ConfigurationParser(warnings);
        Configuration config = configParser.parseConfiguration(in);

        DefaultShellCallback callback = new DefaultShellCallback(overwrite);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);

        // 输出警告信息
        System.out.println("[warn Info]:");
        for (String warning : warnings) {
            System.out.println(warning);
        }
    }
}