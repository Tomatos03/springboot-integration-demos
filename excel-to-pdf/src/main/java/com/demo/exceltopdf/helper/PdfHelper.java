package com.demo.exceltopdf.helper;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.demo.exceltopdf.config.LibreOfficeProperties;
import com.demo.exceltopdf.util.PdfUtil;
import com.demo.fesodeexcel.model.SheetData;
import com.demo.fesodeexcel.util.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Excel → PDF 转换的流式 API 门面（懒加载）：以「Excel 来源」为参数创建链，
 * 配置阶段不做任何 IO 与生成，只有终端操作才真正构建——读模板 →
 * {@link ExcelUtil} 生成多 Sheet Excel → {@link PdfUtil} 经 LibreOffice 转 PDF；
 * 首次构建结果缓存在链实例上，多次终端操作不重复转换。
 *
 * <p>三种入口（靠方法名与重载区分）：
 * <ul>
 *   <li>{@link #template(String)} / {@link #template(byte[])}——含占位符的模板
 *       （classpath 位置或字节），需追加 {@link SheetData} 填充；</li>
 *   <li>{@link #excel(byte[])} / {@link #excel(File)}——已填充好的 xlsx，直接转 PDF。</li>
 * </ul>
 *
 * <p>类型即能力边界：excel 入口返回 {@link PdfChain}（终端出口 + 公共流式配置，
 * 无填充方法），template 入口返回 {@link PdfTemplateChain}（再叠加 Sheet 填充），
 * 终端 / 配置 / 填充的详细语义见 {@link PdfTerminal}、{@link PdfChain} 与
 * {@link PdfTemplateChain}。模板至少填充一个 Sheet 的约束类型无法表达，
 * 「至少一个」在懒构建时校验并抛出。
 *
 * <p>用法示例：
 * <pre>{@code
 * // 脱离 Spring：显式传 LibreOffice 配置（默认自动探测 soffice、A4、120 秒）
 * PdfHelper pdfHelper = new PdfHelper(new LibreOfficeProperties());
 *
 * // classpath 模板位置 → 生成 → 导出到浏览器（attachment 下载）
 * pdfHelper.template("excel-template/template.xlsx")
 *          .appendSheets(List.of(SheetData.of("一月", variables, rows)))
 *          .toBrowser(response, "报表.pdf");
 *
 * // 模板字节数组 → 内存字节 / 目录落盘（自动命名）
 * byte[] pdf = pdfHelper.template(templateBytes).appendSheets(sheets).toByteArray();
 * pdfHelper.template(templateBytes).appendSheets(sheets).toDirectory(Path.of("out"));
 *
 * // 已有数据的 Excel → 直接转换（无填充方法）
 * pdfHelper.excel(xlsxBytes).toByteArray();
 * // 命名是链的公共属性：前缀对字节出口同样生效，文件名 = <前缀>-<随机后缀>.pdf
 * pdfHelper.excel(xlsxBytes).fileNamePrefix("月度报表").toByteArray();
 * // 目录落盘：自动命名并带上前缀，目录不存在会自动创建
 * Path written = pdfHelper.excel(xlsxFile).fileNamePrefix("月度报表")
 *                         .toDirectory(Path.of("out"));
 * }</pre>
 *
 * <p>LibreOffice 转换配置（soffice 定位、超时、纸张，{@link LibreOfficeProperties}）
 * 在构造本门面时传入，成为其创建的链的固定配置：Spring 场景注入
 * {@link com.demo.exceltopdf.config.PdfHelperConfiguration} 提供的 bean
 * （携带 application.yml 的 {@code excel-to-pdf.libreoffice.*}）；脱离 Spring 时
 * {@code new PdfHelper(new LibreOfficeProperties())} 即内置默认。
 *
 * <p>链不可变：所有链式方法返回携带完整状态快照的新实例，懒构建缓存不会因
 * 后续配置而失效；字节与 Sheet 入参在入口拷贝快照，调用方后续修改不影响链。
 *
 * @author Tomatos
 */
public final class PdfHelper {

    /** LibreOffice 转换配置：构造器传入，创建链时快照给 Chain，本实例所有链共享 */
    private final LibreOfficeProperties properties;

    /**
     * 以显式的 LibreOffice 转换配置创建门面：此后本实例创建的每一条链都使用该
     * 配置（Chain 构造时成为快照）。Spring 场景注入
     * {@link com.demo.exceltopdf.config.PdfHelperConfiguration} 提供的 bean；
     * 脱离 Spring 时传 {@code new LibreOfficeProperties()} 即内置默认
     * （自动探测 soffice、A4、120 秒）。
     *
     * @param properties LibreOffice 转换配置
     * @throws NullPointerException properties 为 null 时抛出
     */
    public PdfHelper(LibreOfficeProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties 不能为 null");
    }

    /**
     * 模板入口：模板文件在 classpath 中的位置（含占位符，需追加 SheetData 填充）。
     *
     * <p>真正读取模板推迟到终端操作（经 {@link ExcelUtil} 的 classpath 便捷重载），
     * 入口只做非空校验（转换配置来自本门面实例，见
     * {@link #PdfHelper(LibreOfficeProperties)}）。
     *
     * @param classpathTemplatePath 模板在 classpath 中的位置（如 {@code excel-template/template.xlsx}）
     * @return 模板链（终端操作 + Sheet 追加/整体替换，见 {@link PdfTemplateChain}）
     * @throws IllegalArgumentException classpath 模板位置为 null 或空白时抛出
     */
    public PdfTemplateChain template(String classpathTemplatePath) {
        if (classpathTemplatePath == null || classpathTemplatePath.isBlank()) {
            throw new IllegalArgumentException("classpath 模板位置不能为 null 或空白");
        }
        return new Chain(properties, Kind.TEMPLATE_PATH,
                classpathTemplatePath, null, null, List.of(), null);
    }

    /**
     * 模板入口：Excel 模板字节数组（含占位符，需追加 SheetData 填充）。
     *
     * <p>字节数组在入口拷贝快照，调用方后续修改不影响链。
     *
     * @param templateBytes 模板字节（xlsx 文件内容）
     * @return 模板链（终端操作 + Sheet 追加/整体替换，见 {@link PdfTemplateChain}）
     * @throws IllegalArgumentException templateBytes 为 null 或空数组时抛出
     */
    public PdfTemplateChain template(byte[] templateBytes) {
        if (templateBytes == null) {
            throw new IllegalArgumentException("模板字节数组不能为 null");
        }
        if (templateBytes.length == 0) {
            throw new IllegalArgumentException("模板字节数组不能为空");
        }
        return new Chain(properties, Kind.TEMPLATE_BYTES,
                null, templateBytes.clone(), null, List.of(), null);
    }

    /**
     * 已填充 Excel 入口：已经填好数据的 xlsx 字节，直接转 PDF、不填充。
     *
     * <p>字节数组在入口拷贝快照，调用方后续修改不影响链。
     *
     * @param xlsxBytes 已填充数据的 xlsx 字节
     * @return 链（公共流式配置 + 终端操作，见 {@link PdfChain}；类型上不存在填充方法）
     * @throws IllegalArgumentException xlsxBytes 为 null 或空数组时抛出
     */
    public PdfChain excel(byte[] xlsxBytes) {
        if (xlsxBytes == null) {
            throw new IllegalArgumentException("xlsx 字节数组不能为 null");
        }
        if (xlsxBytes.length == 0) {
            throw new IllegalArgumentException("xlsx 字节数组不能为空");
        }
        return new Chain(properties, Kind.EXCEL_BYTES,
                null, xlsxBytes.clone(), null, List.of(), null);
    }

    /**
     * 已填充 Excel 入口：已经填好数据的 xlsx 文件，直接转 PDF、不填充。
     *
     * <p>文件读取推迟到终端操作（懒加载），入口只保存文件引用。
     *
     * @param xlsxFile 已填充数据的 xlsx 文件
     * @return 链（公共流式配置 + 终端操作，见 {@link PdfChain}；类型上不存在填充方法）
     * @throws NullPointerException xlsxFile 为 null 时抛出
     */
    public PdfChain excel(File xlsxFile) {
        Objects.requireNonNull(xlsxFile, "xlsxFile 不能为 null");
        return new Chain(properties, Kind.EXCEL_FILE,
                null, null, xlsxFile, List.of(), null);
    }

    /**
     * Excel 来源：模板类来源经「模板 + 填充」构建 xlsx，已填充类来源直接转换。
     */
    private enum Kind {

        /** 模板在 classpath 的位置（构建时才读取资源） */
        TEMPLATE_PATH,

        /** 模板字节数组（入口即持有字节快照） */
        TEMPLATE_BYTES,

        /** 已填充数据的 xlsx 字节（入口即持有字节快照） */
        EXCEL_BYTES,

        /** 已填充数据的 xlsx 文件（构建时才读取文件） */
        EXCEL_FILE;
    }

    /**
     * 链实现：一个私有类同时实现两个接口，对外「有哪些方法」由入口的返回类型
     * 收窄决定——template 入口以 {@link PdfTemplateChain} 暴露（公共流式配置 +
     * 填充 + 终端操作），excel 入口以 {@link PdfChain} 暴露（公共流式配置 +
     * 终端操作，无填充方法）。
     */
    private static final class Chain implements PdfTemplateChain {

        /** LibreOffice 转换配置快照（来自门面实例构造参数，终端转换使用） */
        private final LibreOfficeProperties properties;

        /** Excel 来源类型 */
        private final Kind kind;

        /** TEMPLATE_PATH 来源的 classpath 模板位置 */
        private final String templatePath;

        /** TEMPLATE_BYTES / EXCEL_BYTES 来源的字节快照 */
        private final byte[] sourceBytes;

        /** EXCEL_FILE 来源的文件引用 */
        private final File excelFile;

        /** 待填充的 Sheet 列表（仅模板类来源有意义；不可变快照） */
        private final List<SheetData<?, ?>> sheets;

        /** PDF 文档名前缀（可空，null 表示未设置；toDirectory 自动命名用） */
        private final String fileNamePrefix;

        /** 懒构建缓存：首次终端操作生成后复用，多次终端操作只转换一次 */
        private byte[] pdfBytes;

        /**
         * 由门面实例的入口方法（传入实例的转换配置）与 {@link #deriveSheets}
         * （沿用原链配置快照）调用，入参直接归属当前链（字节与列表已由调用方
         * 做好快照）。
         *
         * @param properties LibreOffice 转换配置快照
         * @param kind Excel 来源类型
         * @param templatePath classpath 模板位置（TEMPLATE_PATH 来源）
         * @param sourceBytes 字节快照（TEMPLATE_BYTES / EXCEL_BYTES 来源）
         * @param excelFile xlsx 文件（EXCEL_FILE 来源）
         * @param sheets 待填充的 Sheet 列表快照
         * @param fileNamePrefix PDF 文档名前缀（可空，null 表示未设置）
         */
        private Chain(
            LibreOfficeProperties properties,
            Kind kind,
            String templatePath,
            byte[] sourceBytes,
            File excelFile,
            List<SheetData<?, ?>> sheets,
            String fileNamePrefix
        ) {
            this.properties = properties;
            this.kind = kind;
            this.templatePath = templatePath;
            this.sourceBytes = sourceBytes;
            this.excelFile = excelFile;
            this.sheets = sheets;
            this.fileNamePrefix = fileNamePrefix;
        }

        /**
         * 追加一个 Sheet 的填充内容。
         *
         * <p>本方法只存在于模板链类型 {@link PdfTemplateChain} 上——excel 入口创建的
         * 链以 {@link PdfTerminal} 暴露，编译期就没有本方法。追加后返回携带完整
         * 状态快照的新链，原链不变，可继续追加或调用 {@link PdfTerminal} 的终端操作。
         *
         * @param sheetData Sheet 名称 + {@code {xxx}} 变量数据 + {@code {.xxx}} 列表数据
         * @return 追加后的新 Chain（不可变快照，原链不变）
         * @throws NullPointerException sheetData 为 null 时抛出
         */
        @Override
        public Chain appendSheet(SheetData<?, ?> sheetData) {
            Objects.requireNonNull(sheetData, "sheetData 不能为 null");
            List<SheetData<?, ?>> appended = new ArrayList<>(sheets);
            appended.add(sheetData);
            return deriveSheets(appended);
        }

        /**
         * 追加一批 Sheet 的填充内容，顺序即最终 Excel 的 Sheet 顺序。
         *
         * @param sheetList Sheet 填充内容列表，顺序即最终 Excel 的 Sheet 顺序
         * @return 追加后的新 Chain（不可变快照，原链不变）
         * @throws NullPointerException sheetList 或其元素为 null 时抛出
         */
        @Override
        public Chain appendSheets(List<? extends SheetData<?, ?>> sheetList) {
            Objects.requireNonNull(sheetList, "sheets 不能为 null");
            // List.copyOf 校验并拷贝入参（含 null 元素时抛 NPE），再并入既有列表
            List<SheetData<?, ?>> merged = new ArrayList<>(sheets);
            merged.addAll(List.copyOf(sheetList));
            return deriveSheets(merged);
        }

        /**
         * 整体替换（设置）链上的 Sheet 列表：与 {@link #appendSheets(List)} 不同，
         * 本方法不追加，直接用入参替换既有列表。
         *
         * @param sheetList Sheet 填充内容列表（替换后链上的完整列表）
         * @return 替换后的新 Chain（不可变快照，原链不变）
         * @throws NullPointerException sheetList 或其元素为 null 时抛出
         */
        @Override
        public Chain sheets(List<? extends SheetData<?, ?>> sheetList) {
            Objects.requireNonNull(sheetList, "sheets 不能为 null");
            // List.copyOf 同时完成「拒绝 null 元素」与整体替换所需的快照拷贝
            return deriveSheets(List.copyOf(sheetList));
        }

        /**
         * 设置导出 PDF 文档名的前缀（语义见 {@link PdfChain#fileNamePrefix(String)}）。
         * 追加 / 替换 Sheet 后设置前缀同样合法，本方法返回携带完整状态快照的新链，
         * 原链不变。
         *
         * @param prefix PDF 文档名前缀
         * @return 携带新前缀的新 Chain（不可变快照，原链不变）
         * @throws IllegalArgumentException prefix 为 null 或空白时抛出
         */
        @Override
        public Chain fileNamePrefix(String prefix) {
            if (prefix == null || prefix.isBlank()) {
                throw new IllegalArgumentException("文档名前缀不能为 null 或空白");
            }
            return derivePrefix(prefix);
        }

        /**
         * 懒构建后返回 PDF 字节（懒构建语义见 {@link PdfTerminal#toByteArray()}）。
         *
         * @return PDF 文件字节
         */
        @Override
        public byte[] toByteArray() {
            return toPdfBytes();
        }

        /**
         * 随机文件后缀：UUID 全串 36 字符对文件名太长，去连字符后取前 10 位
         * 十六进制作为文件名随机部分。
         *
         * @return 10 位随机十六进制（每次调用现生成）
         */
        private static String randomSuffix() {
            return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        }

        /**
         * 懒构建后把 PDF 以自动生成的文件名写入目录并返回实际路径（懒构建语义见
         * {@link PdfTerminal#toDirectory(Path)}）。文件名按链的文档命名配置生成：
         * {@code [前缀-]<随机后缀>.pdf}，随机后缀每次调用现生成、不覆盖既有文件；
         * 目录不存在会自动创建（含父目录）。
         *
         * @param directory PDF 落盘目录（不存在会自动创建）
         * @return 实际写入的 PDF 文件路径
         * @throws NullPointerException directory 为 null 时抛出
         * @throws IllegalStateException 构建或写盘失败时抛出
         */
        @Override
        public Path toDirectory(Path directory) {
            Objects.requireNonNull(directory, "directory 不能为 null");
            String fileName = (fileNamePrefix == null ? "" : fileNamePrefix + "-")
                    + randomSuffix() + ".pdf";
            try {
                Files.createDirectories(directory);
                return Files.write(directory.resolve(fileName), toPdfBytes());
            } catch (IOException e) {
                throw new IllegalStateException("写入 PDF 目录失败: " + directory, e);
            }
        }

        /**
         * String 便捷重载（懒构建语义见 {@link PdfTerminal#toDirectory(String)}）。
         *
         * @param directoryPath PDF 落盘目录（不存在会自动创建）
         * @return 实际写入的 PDF 文件路径
         * @throws NullPointerException directoryPath 为 null 时抛出
         * @throws IllegalStateException 构建或写盘失败时抛出
         */
        @Override
        public Path toDirectory(String directoryPath) {
            Objects.requireNonNull(directoryPath, "directoryPath 不能为 null");
            return toDirectory(Path.of(directoryPath));
        }

        /**
         * 懒构建后以附件下载形式导出到浏览器
         * （懒构建语义见 {@link PdfTerminal#toBrowser(HttpServletResponse, String)}）。
         *
         * @param response HTTP 响应
         * @param downloadFileName 下载文件名（如 {@code 报表.pdf}，UTF-8 编码进响应头）
         * @throws IllegalArgumentException downloadFileName 为 null 或空白时抛出
         * @throws NullPointerException response 为 null 时抛出
         * @throws IllegalStateException 构建失败或写回响应流失败时抛出
         */
        @Override
        public void toBrowser(HttpServletResponse response, String downloadFileName) {
            Objects.requireNonNull(response, "response 不能为 null");
            if (downloadFileName == null || downloadFileName.isBlank()) {
                throw new IllegalArgumentException("下载文件名不能为 null 或空白");
            }
            // 先构建（成功与否决定是否设置下载头），构建结果与后续终端操作共享缓存
            byte[] pdf = toPdfBytes();
            response.setContentType(MediaType.APPLICATION_PDF_VALUE);
            response.setHeader(
                    HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.attachment()
                                      .filename(downloadFileName, StandardCharsets.UTF_8)
                                      .build()
                                      .toString()
            );
            try {
                OutputStream out = response.getOutputStream();
                out.write(pdf);
                out.flush();
            } catch (IOException e) {
                throw new IllegalStateException("写回浏览器响应流失败", e);
            }
        }

        /**
         * 返回追加了 Sheet 的新链实例（来源、配置与文档名前缀不变）。
         *
         * @param newSheets 新的 Sheet 列表快照
         * @return 新 Chain
         */
        private Chain deriveSheets(List<SheetData<?, ?>> newSheets) {
            return new Chain(properties, kind, templatePath, sourceBytes, excelFile,
                    newSheets, fileNamePrefix);
        }

        /**
         * 返回设置了新文档名前缀的链实例（来源与其余状态不变，Sheet 列表沿用）。
         *
         * @param newPrefix 新的 PDF 文档名前缀
         * @return 新 Chain
         */
        private Chain derivePrefix(String newPrefix) {
            return new Chain(properties, kind, templatePath, sourceBytes, excelFile,
                    sheets, newPrefix);
        }

        /**
         * 懒构建 PDF 字节（加锁保证并发安全）：
         * 首次终端操作执行「生成 xlsx → LibreOffice 转换」并缓存结果。
         *
         * @return PDF 文件字节
         */
        private synchronized byte[] toPdfBytes() {
            if (pdfBytes == null) {
                pdfBytes = PdfUtil.excelToPdf(toExcelBytes(), properties);
            }
            return pdfBytes;
        }

        /**
         * 按来源类型生成转换前的 xlsx 字节（模板来源在此执行 Excel 生成）。
         *
         * @return xlsx 文件字节
         * @throws IllegalStateException 模板未追加 SheetData、模板读取失败或 excel 文件读取失败时抛出
         */
        private byte[] toExcelBytes() {
            switch (kind) {
                case TEMPLATE_PATH:
                    requireSheetsFilled();
                    // 便捷重载在生成时才读取 classpath 模板，模板读取同样是懒加载
                    return ExcelUtil.generateMultiSheet(templatePath, sheets);
                case TEMPLATE_BYTES:
                    requireSheetsFilled();
                    return ExcelUtil.generateMultiSheet(sourceBytes, sheets);
                case EXCEL_BYTES:
                    return sourceBytes;
                case EXCEL_FILE:
                    try {
                        return Files.readAllBytes(excelFile.toPath());
                    } catch (IOException e) {
                        throw new IllegalStateException("读取 Excel 文件失败: " + excelFile, e);
                    }
                default:
                    // 防御：新增来源类型时提醒补全本方法
                    throw new IllegalStateException("未知的 Excel 来源: " + kind);
            }
        }

        /**
         * 校验模板类入口已追加至少一个 SheetData。
         *
         * @throws IllegalStateException 未追加 SheetData 时抛出
         */
        private void requireSheetsFilled() {
            if (sheets.isEmpty()) {
                throw new IllegalStateException("template 入口需要至少追加一个 SheetData"
                        + "（模板含占位符，需要填充才能转 PDF）；若 Excel 已含数据无需填充，"
                        + "请改用 excel(...) 入口");
            }
        }
    }
}
