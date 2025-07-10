# MyBatis Generator 使用说明

## 编写 XML 配置文件

在 `src/main/resources` 目录下创建 `generatorConfig.xml` 文件，内容如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
    PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
    "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">
<generatorConfiguration>
    <context id="DB2Tables" targetRuntime="MyBatis3">
        <jdbcConnection driverClass="com.mysql.cj.jdbc.Driver"
                                        connectionURL="jdbc:mysql://localhost:3306/your_database"
                                        userId="root"
                                        password="password"/>
        <javaModelGenerator targetPackage="com.example.model" targetProject="src/main/java"/>
        <sqlMapGenerator targetPackage="mapper" targetProject="src/main/resources"/>
        <javaClientGenerator type="XMLMAPPER" targetPackage="com.example.mapper" targetProject="src/main/java"/>
        <table tableName="your_table" domainObjectName="YourEntity"/>
    </context>
</generatorConfiguration>
```

更多详细配置可参考 [MyBatis Generator 官方文档](http://mybatis.org/generator/configreference/xmlconfig.html)。

## 使用 MyBatis Generator

本项目支持两种方式使用 MyBatis Genrator：

### 以插件的形式使用 MyBatis Generator

1. 在 `pom.xml` 中添加 MyBatis Generator 插件配置：

    ```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.mybatis.generator</groupId>
                <artifactId>mybatis-generator-maven-plugin</artifactId>
                <version>1.4.2</version>

                <configuration>
                    <configurationFile>src/main/resources/generatorConfig.xml</configurationFile>
                    <overwrite>true</overwrite>
                </configuration>

                <dependencies>
                    <!-- 根据实际的需求修改动成对应的JDBC驱动 -->
                    <dependency>
                        <groupId>org.mariadb.jdbc</groupId>
                        <artifactId>mariadb-java-client</artifactId>
                        <version>3.4.2</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>

    ```

2. 在项目根目录下打开终端运行插件生成代码：

    ```sh
    mvn mybatis-generator:generate
    ```

## 手动创建 Java 类调用 MyBatis Generator

1. 在 `pom.xml` 中添加 MyBatis Generator 依赖：

    ```xml
        <!-- Mybatis-Generator依赖包 -->
        <dependency>
            <groupId>org.mybatis.generator</groupId>
            <artifactId>mybatis-generator-core</artifactId>
            <version>1.4.2</version>
        </dependency>

        <!-- JDBC驱动包 -->
        <dependency>
            <groupId>org.mariadb.jdbc</groupId>
            <artifactId>mariadb-java-client</artifactId>
        </dependency>
    ```

2. 编写 Java 类（如 `MBGGenerator.java`），手动调用 MyBatis Generator API 生成代码。

```java
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

```

3. 运行该 Java 类即可生成代码。
