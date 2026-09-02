package com.demo.exceltopdf.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.demo.exceltopdf.config.LibreOfficeProperties;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * PDF 工具类：基于 LibreOffice headless（无界面）把 xlsx 字节转成 PDF 字节。
 *
 * <p>原理是直接拉起本机的 {@code soffice} 可执行文件：
 * <pre>{@code
 * soffice -env:UserInstallation=file://<临时profile>
 *         --headless --norestore --nolockcheck
 *         --convert-to pdf --outdir <输出目录> <输入.xlsx>
 * }</pre>
 * 转换输出文件与输入同 basename、扩展名换为 {@code .pdf}。
 *
 * <p>要点：
 * <ul>
 *   <li><b>配置入参</b>：soffice 定位、超时、临时目录是否保留、PDF 纸张大小都由调用方传入
 *       {@link LibreOfficeProperties}（Spring 环境注入自 application.yml，测试可直接 new）；</li>
 *   <li><b>PDF 纸张改写</b>：LibreOffice 导出 PDF 没有「纸张大小」参数，纸型只由源 xlsx
 *       每个 Sheet 的页面设置决定，因此转换前先按
 *       {@link LibreOfficeProperties#getPaperSize()}（默认 A4）改写 xlsx，见
 *       {@link #applyConfiguredPaperSize(byte[], LibreOfficeProperties)}；</li>
 *   <li><b>临时 UserInstallation</b>：每次转换用独立 profile 目录，避免与其它
 *       LibreOffice 实例抢锁（首次启动 / 并发转换场景常见问题）；</li>
 *   <li><b>输出与日志落盘</b>：soffice 标准输出/错误重定向到临时日志文件，
 *       退出码非 0 或未找到产物时把日志带回异常信息；</li>
 *   <li><b>串行转换</b>：headless 实例吃 CPU/内存，demo 场景用 {@code synchronized}
 *       串行执行，避免同时拉起多个 soffice；</li>
 *   <li><b>超时兜底</b>：超过 {@link LibreOfficeProperties#getTimeoutSeconds()}
 *       强制结束进程。</li>
 * </ul>
 *
 * <p><b>分页与纸张</b>：PDF 分页遵循每个 Sheet 自己的打印设置（默认按内容分页，
 * 不强制作「一 Sheet 一页」）；纸张大小不属于 LibreOffice 导出参数，转换前会按配置
 * 把各 Sheet 纸型统一改写（默认 A4），保证成品 PDF 的纸张确定。
 *
 * @author Tomatos
 */
@Slf4j
@UtilityClass
public class PDFUtil {

    /** 输入 xlsx 的临时文件名（PDF 产物与其同 basename） */
    private static final String INPUT_FILE_NAME = "input.xlsx";

    /**
     * 把 xlsx 字节转换为 PDF 字节。
     *
     * @param xlsxBytes xlsx 文件字节
     * @param properties LibreOffice 配置（binary 定位、超时、临时目录是否保留）
     * @return pdf 文件字节
     * @throws IllegalStateException 未找到 soffice、转换失败、超时或产物缺失时抛出
     */
    public static synchronized byte[] excelToPdf(byte[] xlsxBytes, LibreOfficeProperties properties) {
        Path workDir = null;
        try {
            // 每次转换一个独立临时工作目录：输入 xlsx、soffice 日志、输出目录、独立 profile
            workDir = Files.createTempDirectory("excel-to-pdf-");
            Path input = workDir.resolve(INPUT_FILE_NAME);
            // 转换前先按配置改写纸张：LibreOffice 导出 PDF 没有纸张参数，纸型只认 xlsx 页面设置
            Files.write(input, applyConfiguredPaperSize(xlsxBytes, properties));

            Path outDir = Files.createDirectories(workDir.resolve("out"));
            Path profileDir = Files.createDirectories(workDir.resolve("lo-profile"));
            Path logFile = workDir.resolve("soffice.log");

            Process process = new ProcessBuilder(buildCommand(properties, profileDir, outDir, input))
                    .redirectErrorStream(true)
                    .redirectOutput(logFile.toFile())
                    .start();

            boolean finished = process.waitFor(properties.getTimeoutSeconds(), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException("LibreOffice 转换超时（超过 "
                        + properties.getTimeoutSeconds() + "s），请检查工作表复杂度或调大 timeout-seconds");
            }
            if (process.exitValue() != 0) {
                throw new IllegalStateException("LibreOffice 转换失败，退出码 " + process.exitValue()
                        + "，日志: " + readLog(logFile));
            }

            Path pdf = outDir.resolve("input.pdf");
            if (!Files.exists(pdf)) {
                throw new IllegalStateException("未找到转换产物 " + pdf + "，日志: " + readLog(logFile));
            }
            return Files.readAllBytes(pdf);
        } catch (IOException e) {
            throw new IllegalStateException("LibreOffice 转换过程发生 IO 异常", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("LibreOffice 转换被中断", e);
        } finally {
            // workDir 为 null 说明在创建临时目录前就失败，无需清理
            if (workDir != null && properties.isKeepWorkFiles()) {
                log.warn("已开启 keep-work-files，保留 LibreOffice 工作目录: {}", workDir.toAbsolutePath());
            } else if (workDir != null) {
                deleteRecursively(workDir);
            }
        }
    }

    /**
     * 按配置改写 xlsx：把每个 Sheet 的页面设置纸型设为目标纸张。
     *
     * <p>LibreOffice 导出 PDF 时不提供「纸张大小」参数，PDF 纸型只由源 xlsx 每个
     * Sheet 的页面设置决定。因此转换前先统一改写纸型（默认 A4），再交给 soffice，
     * 保证成品 PDF 纸张确定；分页与缩放仍遵循源 xlsx 的既有打印设置，本方法不触碰。
     *
     * @param xlsxBytes 源 xlsx 字节
     * @param properties LibreOffice 转换配置（目标纸张取
     *                   {@link LibreOfficeProperties#getPaperSize()}）
     * @return 改写纸型后的 xlsx 字节；未配置目标纸张时原样返回
     */
    private static byte[] applyConfiguredPaperSize(byte[] xlsxBytes, LibreOfficeProperties properties) {
        if (properties.getPaperSize() == null) {
            return xlsxBytes;
        }
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsxBytes));
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                wb.getSheetAt(i).getPrintSetup()
                  .setPaperSize(properties.getPaperSize().getOoxmlCode());
            }
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("按配置改写 xlsx 各 Sheet 纸张大小失败", e);
        }
    }

    /**
     * 探测本机是否可用 LibreOffice（用于测试与运行前自检）。
     *
     * @param properties LibreOffice 配置
     * @return {@code true} 表示能找到 soffice 可执行文件
     */
    public static boolean isLibreOfficeAvailable(LibreOfficeProperties properties) {
        try {
            resolveBinary(properties);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * 组装 soffice 命令行。
     *
     * @param properties LibreOffice 配置（soffice 定位）
     * @param profileDir 本次转换独立的 UserInstallation 目录
     * @param outDir PDF 输出目录
     * @param input 输入 xlsx 路径
     * @return 命令参数列表（ProcessBuilder 直接消费，无需 shell 引号）
     */
    private static List<String> buildCommand(
        LibreOfficeProperties properties,
        Path profileDir,
        Path outDir,
        Path input
    ) {
        List<String> command = new ArrayList<>();
        command.add(resolveBinary(properties));
        // 独立 profile，避免与其它 LibreOffice 实例互抢锁；toUri() 得到 file:///... 形式
        command.add("-env:UserInstallation=" + profileDir.toUri());
        command.add("--headless");
        command.add("--norestore");
        command.add("--nolockcheck");
        command.add("--convert-to");
        command.add("pdf");
        command.add("--outdir");
        command.add(outDir.toString());
        command.add(input.toString());
        return command;
    }

    /**
     * 定位 soffice 可执行文件：配置优先，其次按顺序探测 PATH 中的
     * {@code soffice}、{@code libreoffice}。
     *
     * @param properties LibreOffice 配置
     * @return soffice 的绝对路径或 PATH 中的命令名
     * @throws IllegalStateException 找不到时抛出（附安装指引）
     */
    private static String resolveBinary(LibreOfficeProperties properties) {
        if (properties.getBinary() != null && !properties.getBinary().isBlank()) {
            return properties.getBinary().trim();
        }
        for (String candidate : List.of("soffice", "libreoffice")) {
            Path resolved = findExecutable(candidate);
            if (resolved != null) {
                return resolved.toString();
            }
        }
        throw new IllegalStateException("未找到 LibreOffice（soffice）。请先安装 LibreOffice，"
                + "或在 application.yml 配置 excel-to-pdf.libreoffice.binary 指向 soffice 可执行文件");
    }

    /**
     * 在 PATH 中查找指定名称的可执行文件。
     *
     * @param name 可执行文件名
     * @return 命中的绝对路径；未命中返回 {@code null}
     */
    private static Path findExecutable(String name) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) {
            return null;
        }
        for (String dir : pathEnv.split(File.pathSeparator)) {
            if (dir.isEmpty()) {
                continue;
            }
            Path candidate = Path.of(dir, name);
            if (Files.isRegularFile(candidate) && Files.isExecutable(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * 读取 soffice 运行日志（用于异常信息携带排查线索）。
     *
     * @param logFile 日志文件路径
     * @return 日志内容；文件不存在或读取失败时返回占位文本
     */
    private static String readLog(Path logFile) {
        if (Files.exists(logFile)) {
            try {
                return Files.readString(logFile, StandardCharsets.UTF_8);
            } catch (IOException ignored) {
                // 读取失败不阻断主异常
            }
        }
        return "(无 soffice 日志)";
    }

    /**
     * 递归删除临时目录。
     *
     * @param root 目录根
     */
    private static void deleteRecursively(Path root) {
        try (Stream<Path> paths = Files.walk(root)) {
            // 先删子文件再删目录本身
            paths.sorted(Comparator.reverseOrder())
                 .forEach(path -> {
                     try {
                         Files.deleteIfExists(path);
                     } catch (IOException e) {
                         log.warn("清理临时文件失败: {}", path, e);
                     }
                 });
        } catch (IOException e) {
            log.warn("清理临时目录失败: {}", root, e);
        }
    }
}
