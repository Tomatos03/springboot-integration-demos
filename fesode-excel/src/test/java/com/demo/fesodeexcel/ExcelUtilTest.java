package com.demo.fesodeexcel;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.demo.fesodeexcel.model.ReportRow;
import com.demo.fesodeexcel.model.SheetData;
import com.demo.fesodeexcel.util.ExcelUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 人工检查用测试：模板文件由 {@link ExcelTemplateInitializerTest} 生成，
 * 本类只做「读模板 → 调用 {@link ExcelUtil} 填充 → 输出文件」，
 * 具体填充是否正确由人工打开输出文件核对，不做程序化断言。
 *
 * @author : Tomatos
 * @date : 2026/9/2
 */
class ExcelUtilTest {

    /** 人工检查结果输出目录（相对模块根目录，构建产物不污染源码） */
    private static final Path OUTPUT_DIR = Path.of("target", "excel-manual-check");

    /** 输出文件名 */
    private static final String OUTPUT_FILE = "multi-sheet-filled.xlsx";

    /**
     * 读取模板 → 工具类填充 → 输出 xlsx 供人工核对。
     *
     * @throws IOException 模板缺失或写入失败时抛出
     */
    @Test
    void generateMultiSheetForManualCheck() throws IOException {
        // 模板由 ExcelTemplateInitializerTest 生成；单独跑本测试且模板不存在时跳过
        Path templateFile = Path.of(ExcelTemplateInitializerTest.TEMPLATE_FILE);
        assumeTrue(Files.exists(templateFile), "模板不存在，请先运行 ExcelTemplateInitializerTest 生成模板");
        byte[] templateBytes = Files.readAllBytes(templateFile);

        // 构造两个 Sheet 的填充内容：一月 2 行明细、二月 1 行明细，便于人工对照展开效果
        Map<String, Object> variables = Map.of(
                "title", "2026 年度报表",
                "signer", "报表系统"
        );
        List<SheetData<Map<String, Object>, ReportRow>> sheets = List.of(
                SheetData.of("一月", variables, januaryRows()),
                SheetData.of("二月", variables, februaryRows())
        );

        // 调用工具类：通用 byte[] 入口直接消费内存模板字节并填充
        byte[] filled = ExcelUtil.generateMultiSheet(templateBytes, sheets);

        // 输出到 target 目录供人工检查
        Files.createDirectories(OUTPUT_DIR);
        Path output = OUTPUT_DIR.resolve(OUTPUT_FILE);
        Files.write(output, filled);
        System.out.println("已生成人工检查文件: " + output.toAbsolutePath());
        System.out.println("打开后核对: 标题/制表人占位符已替换、明细逐行展开、底部制表人行仅一份且随明细下移");
    }

    /**
     * 构造「一月」的演示明细（2 行）。
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
     * 构造「二月」的演示明细（1 行）。
     *
     * @return 明细行列表
     */
    private List<ReportRow> februaryRows() {
        return List.of(
                ReportRow.of(1, "王五", "财务部", LocalDate.of(2026, 2, 14), new BigDecimal("3456.75"))
        );
    }
}
