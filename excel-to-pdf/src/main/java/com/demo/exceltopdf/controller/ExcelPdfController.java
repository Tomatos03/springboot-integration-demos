package com.demo.exceltopdf.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.demo.exceltopdf.service.ExcelPdfService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 导出 Demo 控制器：
 * <ul>
 *   <li>{@code GET /excel-pdf/pdf}——生成多 Sheet Excel 并经 LibreOffice 转为 PDF 下载；</li>
 *   <li>{@code GET /excel-pdf/xlsx}——只生成中间产物 Excel 下载，便于与 PDF 对照。</li>
 * </ul>
 *
 * @author Tomatos
 */
@RestController
@RequestMapping("/excel-pdf")
@RequiredArgsConstructor
public class ExcelPdfController {

    /** xlsx 的 MIME 类型 */
    public static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    /** PDF 文件名（下载用） */
    private static final String PDF_FILE_NAME = "demo-multi-sheet.pdf";

    /** xlsx 文件名（下载用） */
    private static final String XLSX_FILE_NAME = "demo-multi-sheet.xlsx";

    private final ExcelPdfService service;

    /**
     * 导出最终 PDF：生成 Excel → LibreOffice 转换（纸张按配置为 A4，分页遵循源 xlsx
     * 打印设置）→ 下载。
     *
     * @param response HTTP 响应（用于设置下载头并承载输出流）
     * @throws IOException 写入响应流失败时抛出
     */
    @GetMapping("/pdf")
    public void exportPdf(HttpServletResponse response) throws IOException {
        setDownloadHeader(response, MediaType.APPLICATION_PDF, PDF_FILE_NAME);
        service.exportDemoPdf(response.getOutputStream());
    }

    /**
     * 导出转换前的中间 Excel，便于与最终 PDF 逐 Sheet 对照。
     *
     * @param response HTTP 响应（用于设置下载头并承载输出流）
     * @throws IOException 写入响应流失败时抛出
     */
    @GetMapping("/xlsx")
    public void exportXlsx(HttpServletResponse response) throws IOException {
        setDownloadHeader(response, XLSX, XLSX_FILE_NAME);
        service.exportDemoXlsx(response.getOutputStream());
    }

    /**
     * 设置附件下载响应头：Content-Type 与 Content-Disposition 文件名（UTF-8 编码）。
     *
     * @param response HTTP 响应
     * @param contentType 下载文件的 MIME 类型
     * @param fileName 下载文件名
     */
    private static void setDownloadHeader(
        HttpServletResponse response,
        MediaType contentType,
        String fileName
    ) {
        response.setContentType(contentType.toString());
        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                                  .filename(fileName, StandardCharsets.UTF_8)
                                  .build()
                                  .toString()
        );
    }
}
