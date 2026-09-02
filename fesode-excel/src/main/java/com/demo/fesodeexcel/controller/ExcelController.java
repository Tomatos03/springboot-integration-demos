package com.demo.fesodeexcel.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.demo.fesodeexcel.service.FesodeExcelService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 导出 Demo 控制器：无参导出静态演示数据生成的多 Sheet Excel。
 *
 * @author fesode
 */
@RestController
@RequestMapping("/excel")
@RequiredArgsConstructor
public class ExcelController {

    /** xlsx 的 MIME 类型 */
    public static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    /** 下载文件名 */
    private static final String FILE_NAME = "demo-multi-sheet.xlsx";

    private final FesodeExcelService service;

    /**
     * 无参导出：service 直接构造静态数据，读模板生成多 Sheet Excel，
     * 字节通过流式重载写入响应输出流。
     *
     * @param response HTTP 响应（用于设置下载头并承载输出流）
     * @throws IOException 写入响应流失败时抛出
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        response.setContentType(XLSX.toString());
        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                                  .filename(FILE_NAME, StandardCharsets.UTF_8)
                                  .build()
                                  .toString()
        );
        service.exportDemo(response.getOutputStream());
    }
}