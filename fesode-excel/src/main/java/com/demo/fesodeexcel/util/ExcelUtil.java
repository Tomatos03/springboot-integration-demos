package com.demo.fesodeexcel.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.demo.fesodeexcel.model.SheetData;
import lombok.experimental.UtilityClass;
import org.apache.fesod.sheet.ExcelWriter;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.write.metadata.WriteSheet;
import org.apache.fesod.sheet.write.metadata.fill.FillConfig;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Excel 模板工具：封装「单 Sheet 模板 → 单文件多 Sheet」的通用生成流程。
 *
 * <p>该流程与业务数据类型无关（{@code {xxx}} 占位符数据用 Map 或 bean，{@code {.xxx}}
 * 列表元素用任意 bean），分两步：
 * <ul>
 *   <li>POI 解析单 Sheet 模板字节（classpath 便捷重载 {@link #generateMultiSheet(String, List)}
 *       负责把模板读成字节），用 {@code cloneSheet} 克隆出 N 个 Sheet
 *       （克隆完整保留样式与占位符），统一命名后序列化为多 Sheet 模板字节；</li>
 *   <li>把多 Sheet 模板字节交给 Fesod（fesod-sheet），对每个 Sheet 依次填充列表占位符
 *       {@code {.xxx}} 与普通变量占位符 {@code {xxx}}，最终写出包含 N 个 Sheet 的 xlsx。</li>
 * </ul>
 *
 * <p>模板自上而下约定为三段，这也是本类支持的通用结构：
 * <ul>
 *   <li><b>顶部固定区</b>：普通变量占位符 {@code {xxx}}（如 {@code {title}}、{@code {date}}），
 *       每 Sheet 只出现一次；</li>
 *   <li><b>中部动态区</b>：列表占位符 {@code {.xxx}} 所在行（可连续多行），填充时随列表
 *       逐行向下重复；</li>
 *   <li><b>底部固定区</b>：与顶部相同的普通变量占位符 {@code {xxx}}（如 {@code {signer}}），
 *       列表展开时整体被下推，不会被逐行重复。</li>
 * </ul>
 * 填充时 {@code {xxx}} 占位符由每个 {@link SheetData} 的 {@code variables} 覆盖，
 * {@code {.xxx}} 占位符由 {@code rows} 列表逐行展开。
 *
 * <p>填充顺序固定为「先列表、后变量」：列表展开先把占位行下方已有的模板行（即底部固定区）
 * 整体下推到最终位置，随后一次变量填充即可覆盖模板中全部 {@code {xxx}} 占位符；列表填充
 * 开启 {@code forceNewRow}，保证每行数据写入新行，而不是覆盖或打散这些既有模板行。
 *
 * <p>每个 Sheet 的填充内容由 {@link SheetData} 承载（Sheet 名称 + {@code {xxx}} 占位符
 * 数据 + {@code {.xxx}} 列表数据），按列表顺序依次处理；空值兜底、默认 Sheet 等业务策略
 * 应留在调用方，本类不感知。
 *
 * @author Tomatos
 */
@UtilityClass
public class ExcelUtil {

    /**
     * 生成一个多 Sheet Excel（克隆 + 填充一步完成）：以「单 Sheet 模板字节」为输入，
     * 克隆出 N 个 Sheet 后逐 Sheet 填充。这是最通用的入口——模板字节可来自 classpath、
     * 上传文件、动态构建等任意来源，其它重载（如
     * {@link #generateMultiSheet(String, List)}）最终都委托本方法。
     *
     * @param templateBytes 单 Sheet 模板的字节（内含占位符与样式）
     * @param sheets 各 Sheet 的填充内容（Sheet 名称、{@code {xxx}} 占位符数据、
     *               {@code {.xxx}} 列表数据）；列表顺序即最终 Excel 的 Sheet 顺序
     * @return xlsx 文件字节数组
     * @throws IllegalArgumentException Sheet 名称重复时抛出
     * @throws IllegalStateException 模板克隆或填充失败时抛出
     */
    public static byte[] generateMultiSheet(
        byte[] templateBytes,
        List<? extends SheetData<?, ?>> sheets
    ) {
        byte[] multiSheetTemplate = cloneToMultiSheetTemplate(templateBytes, sheetNamesOf(sheets));
        return fillMultiSheetTemplate(multiSheetTemplate, sheets);
    }

    /**
     * 便捷重载：从 classpath 读取单 Sheet 模板后，委托
     * {@link #generateMultiSheet(byte[], List)} 完成生成。
     *
     * @param templatePath 单 Sheet 模板在 classpath 中的位置
     * @param sheets 各 Sheet 的填充内容（Sheet 名称、{@code {xxx}} 占位符数据、
     *               {@code {.xxx}} 列表数据）；列表顺序即最终 Excel 的 Sheet 顺序
     * @return xlsx 文件字节数组
     * @throws IllegalArgumentException Sheet 名称重复时抛出
     * @throws IllegalStateException 模板不存在或读取失败时抛出
     */
    public static byte[] generateMultiSheet(
        String templatePath,
        List<? extends SheetData<?, ?>> sheets
    ) {
        InputStream in = ResourceUtil.getStream(templatePath);
        try {
            return generateMultiSheet(IoUtil.readBytes(in), sheets);
        } catch (IORuntimeException e) {
            throw new IllegalStateException("读入模板失败: " + templatePath, e);
        } finally {
            IoUtil.close(in);
        }
    }

    /**
     * 便捷重载：生成后直接把 xlsx 字节写入调用方提供的输出流
     * （如 {@code HttpServletResponse.getOutputStream()} 或文件流），省去中间字节数组。
     *
     * <p>流的所有权在调用方：本方法只写入并 {@code flush}，不会关闭传入的流。
     *
     * @param out 输出流（由调用方持有并负责关闭）
     * @param templatePath 单 Sheet 模板在 classpath 中的位置
     * @param sheets 各 Sheet 的填充内容（Sheet 名称、{@code {xxx}} 占位符数据、
     *               {@code {.xxx}} 列表数据）；列表顺序即最终 Excel 的 Sheet 顺序
     * @throws IllegalArgumentException Sheet 名称重复时抛出
     * @throws IllegalStateException 模板读取失败或写入输出流失败时抛出
     */
    public static void generateMultiSheet(
        OutputStream out,
        String templatePath,
        List<? extends SheetData<?, ?>> sheets
    ) {
        byte[] bytes = generateMultiSheet(templatePath, sheets);
        try {
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            throw new IllegalStateException("写入输出流失败", e);
        }
    }

    /**
     * 提取各 Sheet 的名称（名称个数决定克隆次数与 Sheet 数量）。
     *
     * <p>Sheet 名称必须唯一：遍历时用 {@link Set} 判重，发现重复直接抛
     * {@link IllegalArgumentException}，避免生成同名的歧义 Sheet。
     *
     * @param sheets Sheet 填充内容列表
     * @return Sheet 名称列表
     * @throws IllegalArgumentException Sheet 名称重复时抛出
     */
    private static List<String> sheetNamesOf(List<? extends SheetData<?, ?>> sheets) {
        List<String> names = new ArrayList<>(sheets.size());
        Set<String> seen = new HashSet<>(sheets.size());
        for (SheetData<?, ?> sheet : sheets) {
            String name = sheet.getSheetName();
            if (!seen.add(name)) {
                throw new IllegalArgumentException("Sheet 名称重复: " + name);
            }
            names.add(name);
        }
        return names;
    }

    /**
     * 用 POI 把单 Sheet 模板字节克隆为 N 个 Sheet（完整保留样式与占位符）并统一命名。
     *
     * @param templateBytes 单 Sheet 模板字节
     * @param sheetNames 目标 Sheet 名称（个数决定克隆次数）
     * @return 多 Sheet 待填充模板的工作簿字节
     */
    private static byte[] cloneToMultiSheetTemplate(
        byte[] templateBytes,
        List<String> sheetNames
    ) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(templateBytes))) {

            // 模板只含一个 Sheet，克隆出剩余 N-1 个（完整保留样式与占位符）
            int templateIndex = 0;
            for (int i = 1; i < sheetNames.size(); i++) {
                wb.cloneSheet(templateIndex);
            }

            // 统一 Sheet 命名
            for (int i = 0; i < sheetNames.size(); i++) {
                wb.setSheetName(i, sheetNames.get(i));
            }

            wb.write(out);
        } catch (IOException e) {
            throw new IllegalStateException("克隆多 Sheet 模板失败", e);
        }
        return out.toByteArray();
    }

    /**
     * Fesod：对多 Sheet 模板的每个 Sheet 分别填充 {@code {.xxx}} 列表占位符
     * 与 {@code {xxx}} 变量占位符。
     *
     * @param templateBytes POI 克隆后的多 Sheet 模板字节
     * @param sheets 各 Sheet 的填充内容
     * @return 填充完成的 xlsx 文件字节
     */
    private static byte[] fillMultiSheetTemplate(
        byte[] templateBytes,
        List<? extends SheetData<?, ?>> sheets
    ) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ByteArrayInputStream in = new ByteArrayInputStream(templateBytes);
                ExcelWriter writer = FesodSheet.write(out).withTemplate(in).build()) {

            FillConfig fillConfig = FillConfig.builder()
                                              .forceNewRow(true)
                                              .build();
            for (int i = 0; i < sheets.size(); i++) {
                SheetData<?, ?> sheet = sheets.get(i);
                WriteSheet writeSheet = FesodSheet.writerSheet(i, sheet.getSheetName()).build();

                // 填充 {.xxx} 占位符：rows 列表逐行向下重复（每个元素一行）。forceNewRow
                // 保证每行数据写入新行，占位行下方已有的模板行被整体下推，而不是被覆盖或重复
                if (CollUtil.isNotEmpty(sheet.getRows())) {
                    writer.fill(
                            sheet.getRows(),
                            fillConfig,
                            writeSheet
                    );
                }

                // 填充 {xxx} 占位符：variables（Map 或 bean）一次填充模板中的普通变量占位符
                if (sheet.getVariables() != null) {
                    writer.fill(sheet.getVariables(), writeSheet);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Fesod 填充失败", e);
        }
        return out.toByteArray();
    }
}
