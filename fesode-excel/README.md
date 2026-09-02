# fesode-excel

> 基于 **Apache POI** 与 **Apache Fesod（fesod-sheet）** 实现「单 Sheet 模板动态生成单文件多 Sheet Excel」的 Demo

## 依赖说明

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| Spring Boot | 3.5.3 | 继承自父 POM |
| Apache POI | 5.5.1 | 构建单 Sheet 模板 + 用 `cloneSheet` 克隆出 N 个 Sheet（保留样式与占位符） |
| Apache Fesod | 2.0.2-incubating | `fesod-sheet`，读取克隆后的模板字节并填充占位符（FastExcel 捐赠 Apache 后更名） |
| Hutool | 5.8.39 | 简化字符串/集合判空、classpath 资源读取等样板代码（与 spring-security 模块同版本） |
| Lombok | 继承自父 POM | 简化模型类样板代码（`@Data`、`@NoArgsConstructor`、`@UtilityClass` 等），由根 pom 统一提供 |

> 注意：`fesod-sheet` 底层依赖 POI，本项目显式引入 POI 5.5.1，与 Fesod 2.0.2-incubating 所依赖的 POI 版本一致，避免版本冲突。

## 模板约定

`{xxx}` 变量占位符用于单值替换，`{.xxx}` 列表占位符用于逐行填充

## 实现思路

1. **克隆模板**：POI 读入单 Sheet 模板，用 `cloneSheet` 克隆出 N 个 Sheet 并统一命名（克隆完整保留样式与占位符）；
2. **展开明细**：Fesod 对每个 Sheet 以 `forceNewRow` 逐行填充 `{.xxx}` 列表——每行数据写入新行，占位行下方已有的模板行（底部固定区）被整体下推且只保留一份；
3. **填充变量**：一次性填充 `{xxx}` 变量（如 `{title}`、`{signer}`），覆盖模板中的全部普通变量占位符；
4. **写出文件**：`ExcelWriter.close()` 时把包含 N 个 Sheet 的最终 xlsx 写出。

## 使用示例

```java
// 组装每个 Sheet 的填充内容：T = {xxx} 变量数据（Map 或 bean），rows = {.xxx} 列表（每元素一行）
Map<String, Object> variables = Map.of("title", "2026 年度报表", "signer", "报表系统");

List<SheetData<Map<String, Object>, ReportRow>> sheets = List.of(
        SheetData.of("一月", variables, List.of(
                ReportRow.of(1, "张三", "研发部", LocalDate.of(2026, 1, 5), new BigDecimal("1234.50")))),
        SheetData.of("二月", variables, List.of(
                ReportRow.of(1, "王五", "财务部", LocalDate.of(2026, 2, 14), new BigDecimal("3456.75"))))
);

// 出口一：classpath 模板 → 字节数组（便捷重载）
byte[] bytes = ExcelUtil.generateMultiSheet("excel-template/template.xlsx", sheets);

// 出口二：模板字节 → 字节数组（通用入口，模板可来自上传文件、动态构建等任意来源，其它重载都委托它）
byte[] bytes2 = ExcelUtil.generateMultiSheet(templateBytes, sheets);

// 出口三：classpath 模板 → 直接写输出流（如 HTTP 下载响应；流由调用方持有并负责关闭）
ExcelUtil.generateMultiSheet(response.getOutputStream(), "excel-template/template.xlsx", sheets);
```

## 测试

### 生成Excel到本地目录：

1. 执行 ExcelTemplateInitializerTest 生成单 Sheet 模板（输出到 `src/main/resource/excel-template/template.xlsx`）

```bash
mvn test -Dtest=ExcelTemplateInitializerTest
```

2. 执行 ExcelUtilTest 从模板生成多 Sheet Excel（读模板 → 组装「一月」2 行 +「二月」1 行静态数据 → 调 `ExcelUtil.generateMultiSheet(...)`）

```bash
mvn test -Dtest=ExcelUtilTest
```

3. 查看生成的Excel效果

```bash
# 输出文件位置：
target/excel-manual-check/multi-sheet-filled.xlsx

# 或在终端打开：
open target/excel-manual-check/multi-sheet-filled.xlsx
```

### 导出Excel到浏览器:

1. 启动应用（应用使用默认端口 8080）
```bash
mvn -pl fesode-excel -am spring-boot:run
```

2. 浏览器访问无参导出端点

```bash
# 浏览器访问：
http://localhost:8080/excel/export
# 或在终端执行命令：
curl -OJ http://localhost:8080/excel/export
```

3. 浏览器自动下载导出的Excel
