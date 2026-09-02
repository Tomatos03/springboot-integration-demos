package com.demo.exceltopdf;

import com.demo.exceltopdf.config.LibreOfficeProperties;
import com.demo.exceltopdf.helper.PdfChain;
import com.demo.exceltopdf.helper.PdfHelper;
import com.demo.exceltopdf.helper.PdfTemplateChain;
import com.demo.exceltopdf.service.ExcelPdfService;
import com.demo.exceltopdf.util.PdfUtil;
import com.demo.fesodeexcel.model.ReportRow;
import com.demo.fesodeexcel.model.SheetData;
import com.demo.fesodeexcel.util.ExcelUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * {@link PdfHelper} 流式 API 的人工检查测试：一个测试方法只覆盖一种使用情况，
 * 产物输出到 {@code target/pdf-helper-manual-check/} 供人工打开核对。
 *
 * <p>演示数据与 {@code ExcelPdfService} 保持一致，便于与
 * {@code ExcelPdfManualCheckTest} 的产物对照。依赖 LibreOffice 的用例在本机
 * 未安装时自动跳过；不依赖 LibreOffice 的用例（空 Sheet 校验）任何机器都执行。
 *
 * @author Tomatos
 */
class PdfHelperManualCheckTest {

    /** 人工检查结果输出目录（相对模块根目录，构建产物不污染源码） */
    private static final Path OUTPUT_DIR = Path.of("target", "pdf-helper-manual-check");

    /** 已填充 excel 字节入口用例的 PDF 产物文件名 */
    private static final String PDF_FROM_FILLED_EXCEL = "from-filled-excel.pdf";

    /** excel 入口用例的输入（已填充 xlsx）文件名，落盘供人工对照 */
    private static final String FILLED_XLSX = "filled-demo.xlsx";

    /** 未设前缀时 toDirectory 默认文件名的正则（随机后缀：10 位小写十六进制） */
    private static final String RANDOM_FILE_NAME_REGEX =
            "[0-9a-f]{10}\\.pdf";

    /** 脱离 Spring 使用的门面实例：显式传入 LibreOffice 转换配置 */
    private static PdfHelper pdfHelper;

    /**
     * 准备输出目录，并演示脱离 Spring 的使用方式：直接构造 {@link PdfHelper}
     * 并把 LibreOffice 转换配置作为构造参数传入（内置默认：自动探测 soffice、
     * A4、120 秒）。
     *
     * @throws IOException 创建输出目录失败时抛出
     */
    @BeforeAll
    static void setUp() throws IOException {
        Files.createDirectories(OUTPUT_DIR);
        pdfHelper = new PdfHelper(new LibreOfficeProperties());
    }

    /**
     * 情况：template 入口未追加任何 SheetData 时，链式配置阶段不报错，
     * 错误推迟到终端操作（懒构建）才抛出，并带「改用 excel 入口」的指引。
     *
     * <p>不依赖 LibreOffice（异常在生成 xlsx 之前抛出）。
     */
    @Test
    void templateWithoutSheetsFailsAtTerminalBuild() {
        PdfTemplateChain chain = pdfHelper.template(ExcelPdfService.TEMPLATE_PATH);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                chain::toByteArray
        );
        assertTrue(
                exception.getMessage()
                         .contains("template"),
                "异常应带 template 入口的提示，实际: " + exception.getMessage()
        );
    }

    /**
     * 本机未安装 LibreOffice 时跳过 PDF 转换用例（提示信息供人工处理）。
     */
    private static void assumeLibreOfficeAvailable() {
        assumeTrue(
                PdfUtil.isLibreOfficeAvailable(new LibreOfficeProperties()),
                "本机未找到 soffice，跳过 PDF 转换；安装 LibreOffice 后重跑即可"
        );
    }

    /**
     * 构造与 {@code ExcelPdfService} 演示一致的 Sheet 填充数据。
     *
     * @return 一月/二月两个 Sheet 的填充内容
     */
    private static List<SheetData<Map<String, Object>, ReportRow>> demoSheets() {
        Map<String, Object> variables = Map.of(
                "title",
                "2026 年度报表",
                "signer",
                "报表系统"
        );
        return List.of(
                SheetData.of(
                        "一月",
                        variables,
                        List.of(
                                ReportRow.of(
                                        1,
                                        "张三",
                                        "研发部",
                                        LocalDate.of(2026, 1, 5),
                                        new BigDecimal("1234.50")
                                ),
                                ReportRow.of(
                                        2,
                                        "李四",
                                        "市场部",
                                        LocalDate.of(2026, 1, 9),
                                        new BigDecimal("9876.00")
                                )
                        )
                ),
                SheetData.of(
                        "二月",
                        variables,
                        List.of(
                                ReportRow.of(
                                        1,
                                        "王五",
                                        "财务部",
                                        LocalDate.of(2026, 2, 14),
                                        new BigDecimal("3456.75")
                                )
                        )
                )
        );
    }

    private static SheetData<Map<String, Object>, ReportRow> demoSheet() {
        Map<String, Object> variables = Map.of(
                "title",
                "2026 年度报表",
                "signer",
                "报表系统"
        );

        return SheetData.of(
                "Sheet1",
                variables,
                List.of(
                        ReportRow.of(
                                1,
                                "张三",
                                "研发部",
                                LocalDate.of(2026, 1, 5),
                                new BigDecimal("1234.50")
                        )
                )
        );
    }

    /**
     * 断言目标文件存在且内容为 PDF（校验魔数）。
     *
     * @param pdfFile 产物文件
     * @param label 断言场景描述（拼进失败信息）
     * @throws IOException 文件读取失败时抛出
     */
    private static void assertPdf(Path pdfFile, String label) throws IOException {
        assertTrue(
                isPdf(Files.readAllBytes(pdfFile)),
                label + "应为 PDF 文件，实际内容疑似转换失败"
        );
    }

    /**
     * 校验字节内容是否为 PDF（前 4 字节为 {@code %PDF} 魔数）。
     *
     * @param bytes 待校验字节
     * @return {@code true} 表示以 {@code %PDF} 开头
     */
    private static boolean isPdf(byte[] bytes) {
        if (bytes.length < 4) {
            return false;
        }
        return new String(bytes, 0, 4, StandardCharsets.US_ASCII).startsWith("%PDF");
    }

    /**
     * 情况：模板字节入口 + appendSheets（追加）→ toByteArray 直接得到 PDF 字节。
     *
     * @throws IOException 文件读写失败时抛出
     */
    @Test
    void toByteArrayFromTemplateBytes() throws IOException {
        assumeLibreOfficeAvailable();

        PdfTemplateChain chain = pdfHelper.template(readOwnTemplate())
                                          .appendSheets(
                                                  demoSheets()
                                          );
        byte[] pdf = chain.toByteArray();

        assertTrue(isPdf(pdf), "模板字节入口产物应为 PDF");
    }

    /**
     * 读取本模块自带的单 Sheet 模板字节（作为 ExcelUtil 生成的字节输入）。
     *
     * @return 模板字节
     * @throws IllegalStateException 模板缺失或读取失败时抛出
     */
    private static byte[] readOwnTemplate() {
        try (
                InputStream in = PdfHelperManualCheckTest.class.getResourceAsStream(
                        "/" + ExcelPdfService.TEMPLATE_PATH
                )
        ) {
            if (in == null) {
                throw new IllegalStateException(
                        "classpath 中找不到自带模板: " + ExcelPdfService.TEMPLATE_PATH
                );
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "读取自带模板失败: " + ExcelPdfService.TEMPLATE_PATH,
                    e
            );
        }
    }

    /**
     * 情况：同一链先 toByteArray 再 toDirectory，第二次终端操作复用首次懒构建的
     * 结果——两处字节一致，证明没有重复转换。
     *
     * @throws IOException 文件读写失败时抛出
     */
    @Test
    void secondTerminalOperationReusesLazyBuild() throws IOException {
        assumeLibreOfficeAvailable();

        PdfTemplateChain chain = pdfHelper.template(readOwnTemplate())
                                          .appendSheets(demoSheets())
                                          .appendSheet(demoSheet());
        byte[] pdf = chain.toByteArray();
        Path written = chain.toDirectory(OUTPUT_DIR);

        assertArrayEquals(
                pdf,
                Files.readAllBytes(written),
                "同一条链第二次终端操作应复用首次懒构建的结果（不重复转换）"
        );
        System.out.println("已生成 PDF（人工核对用）: " + written.toAbsolutePath());
    }

    /**
     * 情况：已有数据的 Excel 字节 + excel(byte[]) 入口 → toByteArray 得到 PDF。
     *
     * <p>输入 xlsx 用模板现场生成，同时落盘供人工对照。
     *
     * @throws IOException 文件读写失败时抛出
     */
    @Test
    void toByteArrayFromFilledExcelBytes() throws IOException {
        assumeLibreOfficeAvailable();

        Path xlsx = OUTPUT_DIR.resolve(FILLED_XLSX);
        Files.write(xlsx, buildFilledXlsx());
        System.out.println("已生成已填充 Excel（人工核对用）: " + xlsx.toAbsolutePath());

        byte[] pdf = pdfHelper.excel(Files.readAllBytes(xlsx))
                              .toByteArray();
        assertTrue(isPdf(pdf), "excel 字节直转入口产物应为 PDF");

        Path written = OUTPUT_DIR.resolve(PDF_FROM_FILLED_EXCEL);
        Files.write(written, pdf);
        System.out.println("已生成 PDF（人工核对用）: " + written.toAbsolutePath());
    }

    /**
     * 用自带模板生成一份「已填充数据」的 xlsx 字节（excel 入口用例的输入）。
     *
     * @return xlsx 文件字节
     */
    private static byte[] buildFilledXlsx() {
        return ExcelUtil.generateMultiSheet(readOwnTemplate(), demoSheets());
    }

    /**
     * 情况：已有数据的 Excel 文件 + excel(File) 入口 → toBrowser 以附件下载
     * 形式导出到浏览器（下载头与响应体）。
     *
     * @throws IOException 文件读写失败时抛出
     */
    @Test
    void toBrowserFromFilledExcelFile() throws IOException {
        assumeLibreOfficeAvailable();

        Path xlsx = OUTPUT_DIR.resolve(FILLED_XLSX);
        Files.write(xlsx, buildFilledXlsx());

        MockHttpServletResponse response = new MockHttpServletResponse();
        pdfHelper.excel(xlsx.toFile())
                 .toBrowser(response, "中文报表.pdf");

        assertEquals(
                "application/pdf",
                response.getContentType(),
                "浏览器导出应使用 PDF 类型"
        );
        String disposition = response.getHeader(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(disposition, "浏览器导出应设置 Content-Disposition");
        assertTrue(
                disposition.startsWith("attachment;"),
                "浏览器导出应为附件下载，实际: " + disposition
        );
        assertTrue(
                disposition.contains("filename*=UTF-8''"),
                "中文文件名应 UTF-8 编码进响应头，实际: " + disposition
        );
        assertTrue(isPdf(response.getContentAsByteArray()), "浏览器导出的响应体应为 PDF");
    }

    /**
     * 情况：excel 字节入口（返回 PdfChain）+ fileNamePrefix 设置前缀 → toDirectory
     * 自动命名落盘；目标目录不存在，应被自动创建。
     *
     * @throws IOException 文件读写失败时抛出
     */
    @Test
    void toDirectoryWithPrefixFromFilledExcelBytes() throws IOException {
        assumeLibreOfficeAvailable();

        Path directory = OUTPUT_DIR.resolve("auto-created");
        Path pdf = pdfHelper.excel(buildFilledXlsx())
                            .fileNamePrefix("prefixed")
                            .toDirectory(directory);

        assertTrue(Files.isDirectory(directory), "不存在的目录应被自动创建");
        assertEquals(directory, pdf.getParent(), "产物应写入目标目录");
        String fileName = pdf.getFileName().toString();
        assertTrue(
                fileName.startsWith("prefixed-") && fileName.endsWith(".pdf"),
                "文件名应为 <前缀>-<随机后缀>.pdf，实际: " + fileName
        );
        assertPdf(pdf, "excel 字节 + 前缀自动命名落盘产物");
        System.out.println("已生成 PDF（人工核对用）: " + pdf.toAbsolutePath());
    }

    /**
     * 情况：template 入口 + sheets，不设置 fileNamePrefix → toDirectory 使用默认
     * 生成的 <随机后缀>.pdf 文件名（10 位十六进制，UUID 去连字符取前 10 位）。
     *
     * @throws IOException 文件读写失败时抛出
     */
    @Test
    void toDirectoryWithoutPrefixUsesRandomSuffixName() throws IOException {
        assumeLibreOfficeAvailable();

        Path pdf = pdfHelper.template(ExcelPdfService.TEMPLATE_PATH)
                            .sheets(demoSheets())
                            .toDirectory(OUTPUT_DIR);

        assertTrue(
                pdf.getFileName().toString().matches(RANDOM_FILE_NAME_REGEX),
                "未设前缀时应使用 <随机后缀>.pdf 默认名，实际: " + pdf.getFileName()
        );
        assertPdf(pdf, "template 链默认名落盘产物");
        System.out.println("已生成 PDF（人工核对用）: " + pdf.toAbsolutePath());
    }

    /**
     * 情况：fileNamePrefix 前缀为 null 或空白时，配置期立即抛异常（不等懒构建）。
     *
     * <p>不依赖 LibreOffice（配置阶段校验，不触发构建），任何机器都执行。
     */
    @Test
    void blankFileNamePrefixRejectedAtConfigTime() {
        PdfChain chain = pdfHelper.excel(buildFilledXlsx());

        assertThrows(IllegalArgumentException.class, () -> chain.fileNamePrefix(null));
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> chain.fileNamePrefix(" ")
        );
        assertTrue(
                exception.getMessage().contains("前缀"),
                "异常应带前缀提示，实际: " + exception.getMessage()
        );
    }

    /**
     * 情况：命名配置是链的公共流式操作、与出口无关——excel 链设置 fileNamePrefix
     * 后 toByteArray 依然可用（产出的 PDF 有名字，只是字节本身不携带文件名）。
     *
     * @throws IOException 文件读写失败时抛出
     */
    @Test
    void commonPrefixWorksWithByteArrayTerminal() throws IOException {
        assumeLibreOfficeAvailable();

        byte[] pdf = pdfHelper.excel(buildFilledXlsx())
                              .fileNamePrefix("prefixed-bytes")
                              .toByteArray();

        assertTrue(isPdf(pdf), "设置前缀后的字节出口产物应为 PDF");
    }
}
