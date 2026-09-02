package com.demo.fesodeexcel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 初始化 Demo 的 Excel 模板文件：
 * 将单 Sheet 模板生成到 {@code src/main/resource/excel-template/template.xlsx}，
 * 供 {@link com.demo.fesodeexcel.service.FesodeExcelService} 从 classpath 读入。
 *
 *
 * @author Tomaots
 */
class ExcelTemplateInitializerTest {

    /** 模板输出相对路径（相对模块根目录，与 classpath 对应目录一致） */
    public static final String TEMPLATE_FILE = "src/main/resource/excel-template/template.xlsx";

    /**
     * 生成模板文件到 Demo 资源目录。
     *
     * @throws IOException 写入失败时抛出
     */
    @Test
    void initTemplate() throws IOException {
        File target = new File(TEMPLATE_FILE).getAbsoluteFile();
        File parent = target.getParentFile();
        assertThat(parent).isNotNull();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("无法创建目录: " + parent);
        }

        try (Workbook wb = new XSSFWorkbook(); OutputStream out = new FileOutputStream(target)) {
            writeTemplate(wb.createSheet("Template"), wb);
            wb.write(out);
        }

        assertThat(target).exists();
        assertThat(target.length()).isGreaterThan(0);
    }

    /**
     * 在单张模板 Sheet 上写入标题占位符行、表头行、列表占位符行。
     *
     * @param sheet 模板 Sheet
     * @param wb 工作簿（用于创建样式与字体）
     */
    private void writeTemplate(Sheet sheet, Workbook wb) {
        CellStyle titleStyle = wb.createCellStyle();
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        Font titleFont = wb.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);

        CellStyle headerStyle = wb.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // 标题行：普通变量占位符
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("{title}");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        // 表头行
        String[] headers = {"序号", "姓名", "部门", "日期", "金额"};
        Row headerRow = sheet.createRow(1);
        for (int c = 0; c < headers.length; c++) {
            Cell cell = headerRow.createCell(c);
            cell.setCellValue(headers[c]);
            cell.setCellStyle(headerStyle);
        }

        // 列表占位符行：Fesod 的 {.field} 会在填充列表时逐行向下重复
        Row dataRow = sheet.createRow(2);
        dataRow.createCell(0).setCellValue("{.seq}");
        dataRow.createCell(1).setCellValue("{.name}");
        dataRow.createCell(2).setCellValue("{.department}");
        dataRow.createCell(3).setCellValue("{.date}");
        dataRow.createCell(4).setCellValue("{.amount}");

        // 底部固定区：普通变量占位符行——列表展开时整体下推，不会被逐行重复
        Row footerRow = sheet.createRow(3);
        Cell footerCell = footerRow.createCell(0);
        footerCell.setCellValue("制表人：{signer}");
        footerCell.setCellStyle(headerStyle);

        // 固定列宽
        sheet.setColumnWidth(0, 8 * 256);
        sheet.setColumnWidth(1, 14 * 256);
        sheet.setColumnWidth(2, 16 * 256);
        sheet.setColumnWidth(3, 14 * 256);
        sheet.setColumnWidth(4, 12 * 256);
    }
}
