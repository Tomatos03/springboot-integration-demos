package com.demo.exceltopdf.config;

import com.demo.exceltopdf.util.PdfUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LibreOffice headless 转换相关配置（前缀 {@code excel-to-pdf.libreoffice}）。
 *
 * <p>转换依赖本机已安装 LibreOffice（提供 {@code soffice} 可执行文件）；本类负责
 * 描述「怎么找到它、转换给多久时间、临时文件是否保留、产物 PDF 用多大纸」，
 * 具体调用逻辑见 {@link PdfUtil}。
 *
 * @author Tomatos
 */
@Data
@ConfigurationProperties(prefix = "excel-to-pdf.libreoffice")
public class LibreOfficeProperties {

    /**
     * soffice 可执行文件路径。
     *
     * <p>留空时按顺序自动探测 PATH 中的 {@code soffice}、{@code libreoffice}；
     * 手动安装到非标准位置（如 macOS 的
     * {@code /Applications/LibreOffice.app/Contents/MacOS/soffice}）时显式配置此项。
     */
    private String binary = "";

    /** 单次转换的超时时间（秒），超时则强制终止 soffice 进程 */
    private int timeoutSeconds = 120;

    /**
     * 是否保留转换产生的临时工作目录。
     *
     * <p>默认 {@code false} 转换结束即删除；排查转换失败时可临时置 {@code true}，
     * 保留的目录（含 xlsx 输入与 soffice 日志）路径会打印到应用日志。
     */
    private boolean keepWorkFiles = false;

    /**
     * 产物 PDF 的目标纸张大小。
     *
     * <p>LibreOffice 导出 PDF 没有「纸张大小」参数，PDF 纸型只由源 xlsx 每个 Sheet 的
     * 页面设置决定；因此转换前会按此值把各 Sheet 的页面纸型改写（默认 A4），
     * 保证 PDF 成品的纸张大小确定，而不是跟随 LibreOffice 运行环境的默认纸张。
     */
    private PdfPaperSize paperSize = PdfPaperSize.A4;
}
