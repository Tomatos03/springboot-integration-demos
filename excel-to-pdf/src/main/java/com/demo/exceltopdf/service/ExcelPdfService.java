package com.demo.exceltopdf.service;

import com.demo.exceltopdf.config.LibreOfficeProperties;
import com.demo.exceltopdf.util.PDFUtil;
import com.demo.fesodeexcel.model.ReportRow;
import com.demo.fesodeexcel.model.SheetData;
import com.demo.fesodeexcel.util.ExcelUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Excel 生成 + PDF 转换服务。
 *
 * <p>复用 {@code fesode-excel} 模块的生成代码：
 * <ul>
 *   <li>{@link ExcelUtil#generateMultiSheet(byte[], List)}——把本模块
 *       {@code resources/excel-template/} 下的单 Sheet 模板读成字节，克隆 + Fesod
 *       填充出多 Sheet Excel；</li>
 *   <li>生成的 xlsx 字节再交给 {@link PDFUtil}，由 LibreOffice headless 导出 PDF。
 *       PDF 分页遵循源 xlsx 每个 Sheet 自带的打印设置，本模块不强制作一页一 Sheet；
 *       纸张大小由 {@code excel-to-pdf.libreoffice.paper-size} 控制（默认 A4），
 *       转换前统一改写进各 Sheet 的页面设置。</li>
 * </ul>
 *
 * <p>静态演示数据与 {@code fesode-excel} 的 {@code FesodeExcelService} 保持一致，
 * 便于两份 demo 对照同一份 Excel 内容。
 *
 * @author Tomatos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelPdfService {

    /** 本模块自带的单 Sheet 模板（classpath 路径） */
    public static final String TEMPLATE_PATH = "excel-template/template.xlsx";

    /** 模板中普通变量占位符的键：全局标题 */
    private static final String TITLE_PLACEHOLDER = "title";

    /** 模板中普通变量占位符的键：制表人 */
    private static final String SIGNER_PLACEHOLDER = "signer";

    /** 制表人占位符 {signer} 的默认兜底值 */
    private static final String DEFAULT_SIGNER = "报表系统";

    private final LibreOfficeProperties properties;

    /**
     * 全流程演示：生成多 Sheet Excel → LibreOffice 转 PDF，把 PDF 字节写入输出流。
     *
     * @param out 输出流（所有权在调用方，本方法只写入并 flush）
     */
    public void exportDemoPdf(OutputStream out) {
        write(out, PDFUtil.excelToPdf(buildDemoXlsx(), properties));
    }

    /**
     * 写入输出流并 flush（不关闭流，所有权在调用方）。
     *
     * @param out 输出流
     * @param bytes 待写字节
     */
    private void write(OutputStream out, byte[] bytes) {
        try {
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            throw new IllegalStateException("写入输出流失败", e);
        }
    }

    /**
     * 用静态演示数据生成多 Sheet Excel 字节（复用 fesode-excel 的 ExcelUtil）。
     *
     * @return 生成的多 Sheet xlsx 字节
     */
    private byte[] buildDemoXlsx() {
        Map<String, Object> variables = Map.of(
                TITLE_PLACEHOLDER,
                "2026 年度报表",
                SIGNER_PLACEHOLDER,
                DEFAULT_SIGNER
        );

        List<SheetData<Map<String, Object>, ReportRow>> sheets = List.of(
                SheetData.of("一月", variables, januaryRows()),
                SheetData.of("二月", variables, februaryRows())
        );

        return ExcelUtil.generateMultiSheet(readOwnTemplate(), sheets);
    }

    /**
     * 构造「一月」的静态演示明细（2 行）。
     *
     * @return 明细行列表
     */
    private List<ReportRow> januaryRows() {
        return List.of(
                ReportRow.of(1, "张三", "研发部aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", LocalDate.of(2026, 1, 5),
                             new BigDecimal( "1234.50")),
                ReportRow.of(2, "李四", "市场部", LocalDate.of(2026, 1, 9), new BigDecimal("9876.00"))
        );
    }

    /**
     * 构造「二月」的静态演示明细（1 行）。
     *
     * @return 明细行列表
     */
    private List<ReportRow> februaryRows() {
        return List.of(
                ReportRow.of(1, "王五", "财务部", LocalDate.of(2026, 2, 14), new BigDecimal("3456.75"))
        );
    }

    /**
     * 读取本模块自带的单 Sheet 模板字节（作为 ExcelUtil 生成的字节输入）。
     *
     * @return 模板字节
     * @throws IllegalStateException 模板缺失或读取失败时抛出
     */
    private byte[] readOwnTemplate() {
        try (InputStream in = ExcelPdfService.class.getResourceAsStream("/" + TEMPLATE_PATH)) {
            if (in == null) {
                throw new IllegalStateException("classpath 中找不到自带模板: " + TEMPLATE_PATH);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("读取自带模板失败: " + TEMPLATE_PATH, e);
        }
    }

    /**
     * 只生成多 Sheet Excel（转换前的中间产物），便于与最终 PDF 对照。
     *
     * @param out 输出流（所有权在调用方，本方法只写入并 flush）
     */
    public void exportDemoXlsx(OutputStream out) {
        write(out, buildDemoXlsx());
    }
}
