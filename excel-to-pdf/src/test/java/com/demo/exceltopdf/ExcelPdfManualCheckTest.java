package com.demo.exceltopdf;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.demo.exceltopdf.config.LibreOfficeProperties;
import com.demo.exceltopdf.service.ExcelPdfService;
import com.demo.exceltopdf.util.PdfUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 人工检查用测试：跑通「生成 Excel → LibreOffice 转 PDF」全流程，
 * 产物输出到 {@code target/excel-manual-check/} 供人工打开核对。
 *
 * <p>本 demo 不强制作「一 Sheet 一 PDF 页」：PDF 分页遵循源 xlsx 每个 Sheet 自带的
 * 打印设置；纸张大小按配置（默认 A4）在转换前改写进 xlsx。
 *
 * <p>本机未安装 LibreOffice 时 PDF 转换部分自动跳过（xlsx 仍会生成）。
 *
 * @author Tomatos
 */
class ExcelPdfManualCheckTest {

    /** 人工检查结果输出目录（相对模块根目录，构建产物不污染源码） */
    private static final Path OUTPUT_DIR = Path.of("target", "excel-manual-check");

    /** PDF 输出文件名 */
    private static final String PDF_FILE = "demo-multi-sheet.pdf";

    /** xlsx 输出文件名 */
    private static final String XLSX_FILE = "demo-multi-sheet.xlsx";

    /**
     * 全流程人工核对：
     * <ul>
     *   <li>生成中间 xlsx（不依赖 LibreOffice）；</li>
     *   <li>若本机可用 soffice，再导出 PDF 并做最基本的魔数断言。</li>
     * </ul>
     *
     * @throws IOException 文件读写失败时抛出
     */
    @Test
    void exportPdfForManualCheck() throws IOException {
        Files.createDirectories(OUTPUT_DIR);

        // 直接 new 而非 Spring 注入：本测试不启动应用上下文
        LibreOfficeProperties props = new LibreOfficeProperties();
        ExcelPdfService service = new ExcelPdfService(props);

        // 中间产物 xlsx 总是可生成，先落盘供对照
        Path xlsx = OUTPUT_DIR.resolve(XLSX_FILE);
        try (OutputStream out = Files.newOutputStream(xlsx)) {
            service.exportDemoXlsx(out);
        }
        System.out.println("已生成中间 Excel（人工核对用）: " + xlsx.toAbsolutePath());

        // LibreOffice 未安装时跳过 PDF 部分
        assumeTrue(PdfUtil.isLibreOfficeAvailable(props),
                   "本机未找到 soffice，跳过 PDF 转换；安装 LibreOffice 后重跑即可");

        Path pdf = OUTPUT_DIR.resolve(PDF_FILE);
        try (OutputStream out = Files.newOutputStream(pdf)) {
            service.exportDemoPdf(out);
        }

        byte[] pdfBytes = Files.readAllBytes(pdf);
        assertTrue(pdfBytes.length >= 4, "PDF 内容过短，疑似转换失败");
        String magic = new String(pdfBytes, 0, 4, StandardCharsets.US_ASCII);
        assertTrue(magic.startsWith("%PDF"), "产物应为 PDF，实际魔数: " + magic);
        System.out.println("已生成 PDF（人工核对用）: " + pdf.toAbsolutePath());
        System.out.println("打开核对: 内容与 xlsx 一致、纸张为 A4（可改 paper-size 配置）、"
                + "分页符合源 Excel 打印设置、中文可读");
    }
}
