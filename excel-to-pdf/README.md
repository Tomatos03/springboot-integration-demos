# excel-to-pdf

> 基于 **Apache POI（复用兄弟模块 fesode-excel）** 生成多 Sheet Excel，再由 **PDFUtil（LibreOffice headless）** 转换为 PDF 的 Demo

## 依赖说明

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| Spring Boot | 3.5.3 | 继承自父 POM |
| fesode-excel（兄弟模块） | 1.0-SNAPSHOT | 复用其 `ExcelUtil.generateMultiSheet(...)` 生成多 Sheet Excel；单 Sheet 模板由**本模块自带**；Apache POI / Fesod 由它传递引入（本模块另显式声明同版本 POI，用于改写 xlsx 页面纸型） |
| LibreOffice | 7.x（本机 / 容器） | 无 Java 依赖，`PDFUtil` 以 `soffice --headless` 进程方式完成 Excel → PDF 转换 |

> 注意：`fesode-excel` 原是可独立执行的 fat jar，为了让本模块能把它当普通 jar 依赖，已在
> `fesode-excel/pom.xml` 中给 spring-boot-maven-plugin 增加 `repackage phase=none`
> （与 `spring-security/common` 的做法一致）。它仍可用 `mvn spring-boot:run` 单独运行。

## 前置环境

Ubuntu / Debian（含 CJK 字体，避免中文乱码）：

```bash
sudo apt install libreoffice-calc fonts-noto-cjk
```

验证：

```bash
soffice --version
```

macOS / Windows 安装桌面版 LibreOffice 后，`soffice` 通常不在 PATH，需在 `application.yml`
配置可执行文件路径，例如：

```yaml
excel-to-pdf:
  libreoffice:
    # macOS: /Applications/LibreOffice.app/Contents/MacOS/soffice
    # Windows: C:\\Program Files\\LibreOffice\\program\\soffice.exe
    binary: /Applications/LibreOffice.app/Contents/MacOS/soffice
```

## 实现思路

1. **生成多 Sheet Excel**：复用 `fesode-excel` 的 `ExcelUtil.generateMultiSheet(...)`——先把本模块自带的单 Sheet 模板（`src/main/resources/excel-template/template.xlsx`）读成字节，POI 克隆出 N 个 Sheet 后由 Fesod 逐 Sheet 填充 `{.xxx}` 明细与 `{xxx}` 变量，产物是两个 Sheet 的报表（一月/二月）；
2. **LibreOffice headless 转换**：`util/PDFUtil` 用 `ProcessBuilder` 拉起
   `soffice -env:UserInstallation=file://<独立临时profile> --headless --norestore --nolockcheck --convert-to pdf --outdir <临时目录> <输入.xlsx>`；
   - 每次转换用独立的临时 `UserInstallation`，避免与其它 LibreOffice 实例互抢锁；
   - **PDF 纸型统一为 A4**：LibreOffice 导出 PDF 没有「纸张大小」参数，纸型只由 xlsx 每个
     Sheet 的页面设置决定，因此 `PDFUtil` 转换前用 POI 按 `excel-to-pdf.libreoffice.paper-size`
     （默认 A4）改写各 Sheet 页面纸型；其余分页/缩放仍遵循源 xlsx 打印设置；
   - soffice 输出/日志落盘，失败时把日志带回异常；
   - 转换串行执行 + 超时兜底（默认 120s）；
   - soffice 定位等参数由调用方传入 `LibreOfficeProperties`（Spring 注入 application.yml）；
3. **下载 PDF**：控制器把 PDF 字节写回响应流（attachment 下载）。

## 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/excel-pdf/pdf` | 全流程：生成 Excel → LibreOffice 转 PDF 并下载 |
| GET | `/excel-pdf/xlsx` | 只下载转换前的中间 Excel，便于与 PDF 对照 |

### 导出 PDF

```bash
# 浏览器访问：
http://localhost:8080/excel-pdf/pdf
# 或在终端执行命令：
curl -OJ http://localhost:8080/excel-pdf/pdf
```

### 导出转换前的 Excel

```bash
curl -OJ http://localhost:8080/excel-pdf/xlsx
```

## 配置

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `server.port` | 8080 | 应用端口 |
| `excel-to-pdf.libreoffice.binary` | 空（自动探测） | `soffice` 路径；留空按 `soffice` → `libreoffice` 顺序找 PATH |
| `excel-to-pdf.libreoffice.timeout-seconds` | 120 | 单次转换超时（秒） |
| `excel-to-pdf.libreoffice.keep-work-files` | false | 是否保留临时工作目录（排错用，开启后路径打印到日志） |
| `excel-to-pdf.libreoffice.paper-size` | A4 | 转换产物 PDF 的纸张大小（`A4` / `LETTER`）；LibreOffice 导出无纸张参数，转换前改写进各 Sheet 的页面设置 |

## 测试

### 本地转换Excel为Pdf

先安装被依赖的兄弟模块 `fesode-excel`（已关闭 repackage，作为普通 jar 进本地仓库）：

```bash
mvn -pl fesode-excel -am install -DskipTests
```
运行测试类

```bash
mvn -pl excel-to-pdf -am test -Dtest=ExcelPdfManualCheckTest
```

输出到 `target/excel-manual-check/`：

```bash
target/excel-manual-check/demo-multi-sheet.xlsx   # 转换前的中间 Excel
target/excel-manual-check/demo-multi-sheet.pdf    # 转换后的 PDF
```

打开 PDF 核对：内容与 xlsx 一致、纸张为 A4、中文可读、分页符合源 Excel 打印设置。

### 容器化运行

使用 docker compose 运行项目容器配置：

```bash
# 方式一：进入 compose 所在目录执行
cd excel-to-pdf/docs/docker && docker compose up -d --build

# 方式二：在仓库根用 -f 指定
docker compose -f excel-to-pdf/docs/docker/compose.yml up -d --build
```
验证PDF导出是否正常

```bash
# 使用curl下载 PDF
curl -OJ http://localhost:8080/excel-pdf/pdf
# 或直接打开浏览器访问
http://localhost:8080/excel-pdf/pdf
```
