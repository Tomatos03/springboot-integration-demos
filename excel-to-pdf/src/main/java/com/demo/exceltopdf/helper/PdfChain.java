package com.demo.exceltopdf.helper;

/**
 * PdfHelper 链的公共接口：在 {@link PdfTerminal} 的终端出口能力之上，叠加
 * 链级的「公共流式配置」。入口创建的链（无论哪种来源）都以本接口（或继承它的
 * 类型）对外暴露，配置阶段零 IO、零生成，懒构建语义见 {@link PdfHelper}。
 *
 * <p>当前唯一的公共流式操作是 PDF 文档命名：
 * {@link #fileNamePrefix(String)} 设置文档名前缀，导出文档名 =
 * {@code [前缀-]<随机后缀>.pdf}，未设置时只有 {@code <随机后缀>.pdf}
 * （随机后缀规则见 {@link #toDirectory(java.nio.file.Path)}）。
 * 文档名是链的固有属性，不因出口不同而消失：字节出口 {@code toByteArray()}
 * 产出的字节本身不携带文件名，那份 PDF 仍是有名字的；目录落盘出口
 * {@link #toDirectory(java.nio.file.Path)} 用它自动命名；{@code toBrowser(...)}
 * 的下载文件名来自调用方参数，不受本配置影响。
 *
 * <p>类型即能力边界：本接口只声明公共流式配置，不包含 Sheet 填充方法——
 * {@link PdfHelper#excel(byte[])} / {@link PdfHelper#excel(java.io.File)}
 * 入口返回本接口（初始声明阶段就得到「链」，而非只有终端的 {@link PdfTerminal}）；
 * 模板链 {@link PdfTemplateChain} 继承本接口并再叠加填充方法。两种链上
 * fileNamePrefix 的可用性一致，见 {@link PdfTemplateChain#fileNamePrefix(String)}。
 *
 * @author Tomatos
 */
public interface PdfChain extends PdfTerminal {

    /**
     * 设置导出 PDF 文档名的前缀：导出文档名 = {@code [前缀-]<随机后缀>.pdf}，
     * 未设置时只有 {@code <随机后缀>.pdf}（随机后缀的规则见
     * {@link #toDirectory(java.nio.file.Path)}）。
     *
     * <p>命名是链的公共属性，与具体出口无关——字节出口下 PDF 仍是一份
     * 「有名字」的文档，目录落盘出口用它自动命名。本方法返回携带完整状态
     * 快照的新链，原链不变，可继续追加 Sheet（模板链）或调用终端操作。
     *
     * @param prefix PDF 文档名前缀（如 {@code 月度报表}，导出名 = {@code 月度报表-<随机后缀>.pdf}）
     * @return 携带新前缀的新链（不可变快照，原链不变）
     * @throws IllegalArgumentException prefix 为 null 或空白时抛出
     */
    PdfChain fileNamePrefix(String prefix);
}
