# excel-to-pdf

> 基于 **Apache POI（复用兄弟模块 fesode-excel）** 生成多 Sheet Excel，再由 **PdfUtil（LibreOffice headless）** 转换为 PDF 的 Demo

## 依赖说明

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| Spring Boot | 3.5.3 | 继承自父 POM |
| fesode-excel（兄弟模块） | 1.0-SNAPSHOT | 复用其 `ExcelUtil.generateMultiSheet(...)` 生成多 Sheet Excel；单 Sheet 模板由**本模块自带**；Apache POI / Fesod 由它传递引入（本模块另显式声明同版本 POI，用于改写 xlsx 页面纸型） |
| LibreOffice | 7.x（本机 / 容器） | 无 Java 依赖，`PdfUtil` 以 `soffice --headless` 进程方式完成 Excel → PDF 转换 |

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
2. **LibreOffice headless 转换**：`util/PdfUtil` 用 `ProcessBuilder` 拉起
   `soffice -env:UserInstallation=file://<独立临时profile> --headless --norestore --nolockcheck --convert-to pdf --outdir <临时目录> <输入.xlsx>`；
   - 每次转换用独立的临时 `UserInstallation`，避免与其它 LibreOffice 实例互抢锁；
   - **PDF 纸型统一为 A4**：LibreOffice 导出 PDF 没有「纸张大小」参数，纸型只由 xlsx 每个
     Sheet 的页面设置决定，因此 `PdfUtil` 转换前用 POI 按 `excel-to-pdf.libreoffice.paper-size`
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

## PdfHelper 封装类

Pdfhelper类 封装了Excel转PDF的整个流程, 对外提供链式调用api配置转换过程。

三种 Excel 来源（靠方法名与重载区分）：

| 来源 | 入口 | 说明 |
| --- | --- | --- |
| classpath 模板位置 | `template(String)` | 模板含 `{xxx}` / `{.xxx}` 占位符，需填充 |
| 模板字节数组 | `template(byte[])` | 同上，字节来自上传 / 缓存等场景 |
| 已有数据的 Excel | `excel(byte[])` / `excel(File)` | 已填充好的 xlsx，直接转 PDF，不填充 |

链类型分三层，入口返回类型即能力边界（详见下方注意点）：

- `PdfTerminal`——终端出口能力（`toByteArray` / `toDirectory` / `toBrowser`）；
- `PdfChain extends PdfTerminal`——在终端出口之上叠加**公共流式配置**（PDF 文档
  命名），**excel 入口的返回类型**（初始声明阶段就得到「链」）；
- `PdfTemplateChain extends PdfChain`——再叠加 Sheet 填充（`appendSheet` /
  `appendSheets` / `sheets`），template 入口的返回类型。

链级公共流式操作（两种入口均可调用）：

| 公共流式操作 | 行为 |
| --- | --- |
| `fileNamePrefix(prefix)` | 设置 PDF 文档名前缀，导出文档名 = `[前缀-]<随机后缀>.pdf`（未设置时只有 `<随机后缀>.pdf`；随机后缀 = UUID 去连字符后的前 10 位十六进制）。命名是链的公共属性、与出口无关：字节出口下 PDF 仍是一份有名字的文档；`toBrowser` 下载文件名来自参数，不受影响 |

终端操作（三种来源均可调用）：

| 终端操作 | 行为 |
| --- | --- |
| `toByteArray()` | 懒构建后返回 PDF 字节 |
| `toDirectory(Path)` / `toDirectory(String)` | 自动命名写盘：文件名 `[前缀-]<随机后缀>.pdf`（10 位十六进制，UUID 前 10 位；每次调用现生成），目录不存在会自动创建，返回实际路径 |
| `toBrowser(response, fileName)` | 设置 PDF 下载头（文件名 UTF-8 编码）并写回响应流 |

```java
// LibreOffice 转换配置在构造 PdfHelper 时显式传入
// （Spring 场景注入 config/PdfHelperConfiguration 提供的 PdfHelper bean 即可）
PdfHelper pdfHelper = new PdfHelper(new LibreOfficeProperties());

// classpath 模板位置 → 追加 Sheet 填充 → 导出到浏览器
pdfHelper.template(ExcelPdfService.TEMPLATE_PATH)
         .appendSheets(sheets)   // List<SheetData<?, ?>>，顺序即最终 Excel 的 Sheet 顺序
         .toBrowser(response, "demo-multi-sheet.pdf");

// 模板字节数组 → 内存字节
// （sheets 为整体替换/设置：直接用入参替换链上已有的 Sheet 列表，非追加；
//   刚创建的链上尚未追加过，与 appendSheets 等价）
byte[] pdf = pdfHelper.template(templateBytes)
                      .sheets(sheets)
                      .toByteArray();

// 模板字节数组 → 追加 Sheet → 目录落盘（自动命名，目录不存在会自动创建）
pdfHelper.template(templateBytes)
         .appendSheets(sheets)
         .toDirectory(Path.of("out"));

// 已有数据的 Excel（无填充方法）→ 目录落盘
pdfHelper.excel(new File("报表.xlsx"))
         .toDirectory(Path.of("out"));

// 命名是链的公共属性：excel 入口返回 PdfChain，前缀对字节出口同样生效
// （这份 PDF 的名字是「月度报表-<随机后缀>.pdf」，只是字节不携带文件名）
byte[] namedBytes = pdfHelper.excel(xlsxBytes)
                             .fileNamePrefix("月度报表")
                             .toByteArray();

// 前缀同样作用于目录落盘：<前缀>-<随机后缀>.pdf，目录不存在会自动创建
Path written = pdfHelper.excel(new File("报表.xlsx"))
                        .fileNamePrefix("月度报表")
                        .toDirectory(Path.of("out"));
```

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


### PdfHelper 类测试

```bash
mvn -pl excel-to-pdf -am test -Dtest=PdfHelperManualCheckTest
```

输出到 `target/pdf-helper-manual-check/`：

```bash
filled-demo.xlsx                # 转换前的已填充 Excel（人工对照）
<随机后缀>.pdf                     # 目录自动命名落盘（默认名；设前缀为 <前缀>-<随机后缀>.pdf）
from-filled-excel.pdf           # 已填充 Excel 字节入口产物（toByteArray 后手写落盘）
auto-created/<前缀>-<随机后缀>.pdf  # 前缀 + toDirectory：目录自动创建
```

打开核对：PDF 内容与 xlsx 一致、纸张为 A4、中文可读。两个人工检查测试可一起跑：

```bash
mvn -pl excel-to-pdf -am test -Dtest=ExcelPdfManualCheckTest,PdfHelperManualCheckTest
```

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
