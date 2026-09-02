package com.demo.fesodeexcel.service;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.demo.fesodeexcel.model.ReportRow;
import com.demo.fesodeexcel.model.SheetData;
import com.demo.fesodeexcel.util.ExcelUtil;
import org.springframework.stereotype.Service;

/**
 * 报表导出服务：构造静态演示数据，读模板生成多 Sheet Excel 并写入输出流。
 *
 * <p>「读入单 Sheet 模板 → POI 克隆多 Sheet → Fesod 填充占位符」的通用流程已抽取到
 * {@link ExcelUtil}；模板结构（三段式）与填充规则见其类注释。
 *
 * @author Tomaots
 */
@Service
public class FesodeExcelService {

    /** 单 Sheet 模板在 classpath 中的位置 */
    public static final String TEMPLATE_PATH = "excel-template/template.xlsx";

    /** 模板中普通变量占位符的键：全局标题 */
    private static final String TITLE_PLACEHOLDER = "title";

    /** 模板中普通变量占位符的键：制表人 */
    private static final String SIGNER_PLACEHOLDER = "signer";

    /** 制表人占位符 {signer} 的默认兜底值 */
    private static final String DEFAULT_SIGNER = "报表系统";

    /**
     * 用静态演示数据生成多 Sheet Excel，写入调用方提供的输出流。
     *
     * @param out 输出流（流的所有权在调用方，本方法只写入并 flush）
     */
    public void exportDemo(OutputStream out) {
        // {xxx} 普通变量占位符数据：标题与制表人
        Map<String, Object> variables = Map.of(
                TITLE_PLACEHOLDER,
                "2026 年度报表",
                SIGNER_PLACEHOLDER,
                DEFAULT_SIGNER
        );

        // 静态演示数据：两个 Sheet 明细行数不同，便于对照动态区展开与底部固定区下推
        List<SheetData<Map<String, Object>, ReportRow>> sheets = List.of(
                SheetData.of("一月", variables, januaryRows()),
                SheetData.of("二月", variables, februaryRows())
        );

        // 读 classpath 模板生成多 Sheet Excel，直接把字节写入输出流
        ExcelUtil.generateMultiSheet(out, TEMPLATE_PATH, sheets);
    }

    /**
     * 构造「一月」的静态演示明细（2 行）。
     *
     * @return 明细行列表
     */
    private List<ReportRow> januaryRows() {
        return List.of(
                ReportRow.of(1, "张三", "研发部", LocalDate.of(2026, 1, 5), new BigDecimal("1234.50")),
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
}
