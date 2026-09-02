package com.demo.exceltopdf.helper;

import java.nio.file.Path;

import jakarta.servlet.http.HttpServletResponse;

/**
 * PdfHelper 链的「终端出口」能力基座：任何来源（template / excel）创建的链都具备
 * 以下三个出口，方法语义一致：
 * <ul>
 *   <li>{@link #toByteArray()}——懒构建后取 PDF 字节；</li>
 *   <li>{@link #toDirectory(Path)} / {@link #toDirectory(String)}——懒构建后按链上的
 *       文档命名配置自动生成文件名，写入目录并返回实际路径；</li>
 *   <li>{@link #toBrowser(HttpServletResponse, String)}——懒构建后以附件下载导出到浏览器。</li>
 * </ul>
 * 「懒构建」指链式配置阶段零 IO、零生成，第一次终端操作才执行完整构建
 * （读模板 → 生成 Excel → LibreOffice 转 PDF），结果缓存在链实例上，
 * 同一条链的多次终端操作不重复转换。完整说明与三种来源入口见 {@link PdfHelper}，
 * 出口之上的公共流式配置（如 PDF 文档命名）见 {@link PdfChain}。
 *
 * <p>类型即能力边界：本接口只声明终端出口，不含 Sheet 填充方法——excel 入口
 * 创建的「已填充数据」链对外类型是继承本接口的 {@link PdfChain}（公共流式配置 +
 * 终端出口，同样没有填充方法），因此「无需填充却调用 appendSheet/appendSheets」
 * 在编译期即被拦截。
 *
 * @author Tomatos
 */
public interface PdfTerminal {

    /**
     * 懒构建后返回 PDF 字节。
     *
     * <p>首次调用执行完整构建（读模板 → 生成 Excel → LibreOffice 转 PDF），
     * 结果缓存在当前链实例上，同一条链的后续终端操作直接复用。
     *
     * @return PDF 文件字节
     * @throws IllegalStateException 模板未追加 SheetData、模板缺失、读取失败或转换失败时抛出
     */
    byte[] toByteArray();

    /**
     * 懒构建后把 PDF 以自动生成的文件名写入目录并返回实际路径。
     *
     * <p>文件名按链上的文档命名配置（前缀，见 {@link PdfChain#fileNamePrefix(String)}）
     * 生成：设置过前缀为 {@code <前缀>-<随机后缀>.pdf}，未设置为 {@code <随机后缀>.pdf}。
     * 随机后缀为 10 位十六进制（UUID 去连字符后取前 10 位），每次调用现生成、
     * 不覆盖既有文件。目录不存在会自动创建（含父目录）。
     *
     * @param directory PDF 落盘目录（不存在会自动创建）
     * @return 实际写入的 PDF 文件路径
     * @throws NullPointerException directory 为 null 时抛出
     * @throws IllegalStateException 构建或写盘失败时抛出
     */
    Path toDirectory(Path directory);

    /**
     * String 便捷重载：懒构建后把 PDF 以自动生成的文件名写入目录并返回实际路径。
     *
     * @param directoryPath PDF 落盘目录（不存在会自动创建）
     * @return 实际写入的 PDF 文件路径
     * @throws NullPointerException directoryPath 为 null 时抛出
     * @throws IllegalStateException 构建或写盘失败时抛出
     */
    Path toDirectory(String directoryPath);

    /**
     * 懒构建后以附件下载形式导出到浏览器。
     *
     * <p>设置 {@code application/pdf} Content-Type 与 attachment Content-Disposition
     * （文件名 UTF-8 编码），把 PDF 字节写入响应流并 flush；流的关闭由 Servlet 容器负责，
     * 本方法不关闭。转换失败时会在写回前抛出，此时尚未设置下载头。
     *
     * @param response HTTP 响应
     * @param downloadFileName 下载文件名（如 {@code 报表.pdf}，UTF-8 编码进响应头）
     * @throws IllegalArgumentException downloadFileName 为 null 或空白时抛出
     * @throws NullPointerException response 为 null 时抛出
     * @throws IllegalStateException 构建失败或写回响应流失败时抛出
     */
    void toBrowser(HttpServletResponse response, String downloadFileName);
}
