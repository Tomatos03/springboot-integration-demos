package com.demo.exceltopdf.helper;

import java.util.List;

import com.demo.fesodeexcel.model.SheetData;

/**
 * 模板链类型：在 {@link PdfChain}（终端出口 + PDF 文档命名等公共流式配置）之上，
 * 再叠加「Sheet 填充」，是 {@link PdfHelper#template(String)} /
 * {@link PdfHelper#template(byte[])} 两个模板入口的返回类型。
 *
 * <p>与 excel 入口（返回 {@link PdfChain}）的区别就是类型本身：本接口上的
 * {@code appendSheet/appendSheets/sheets} 方法对「已填充数据」的 excel 链不存在——
 * 调用即编译错误，「无需（也不能）再填充」由类型系统保证，而不是留到运行时抛异常。
 *
 * <p>「懒构建」语义与终端出口见 {@link PdfTerminal}，公共流式配置见 {@link PdfChain}，
 * 完整说明与入口见 {@link PdfHelper}。注意：类型无法表达「至少追加一个 Sheet」，
 * 空列表仍会在终端操作（懒构建）时校验并抛出，见 {@link #appendSheets(List)}。
 *
 * @author Tomatos
 */
public interface PdfTemplateChain extends PdfChain {

    /**
     * 追加一个 Sheet 的填充内容。
     *
     * <p>追加后返回携带完整状态快照的新链（本接口类型），原链不变，可继续追加
     * 或调用 {@link PdfTerminal} 的终端操作。
     *
     * @param sheetData Sheet 名称 + {@code {xxx}} 变量数据 + {@code {.xxx}} 列表数据
     * @return 追加后的新 PdfTemplateChain
     * @throws NullPointerException sheetData 为 null 时抛出
     */
    PdfTemplateChain appendSheet(SheetData<?, ?> sheetData);

    /**
     * 追加一批 Sheet 的填充内容，顺序即最终 Excel 的 Sheet 顺序。
     *
     * @param sheets Sheet 填充内容列表
     * @return 追加后的新 PdfTemplateChain
     * @throws NullPointerException sheets 或其元素为 null 时抛出
     */
    PdfTemplateChain appendSheets(List<? extends SheetData<?, ?>> sheets);

    /**
     * 整体替换（设置）链上的 Sheet 填充内容：直接用传入列表替换先前追加的列表，
     * 而不是追加。
     *
     * <p>与 {@link #appendSheets(List)} 的区别在于是否保留既有内容：刚创建、尚未
     * 追加任何 Sheet 的链上两者等价；已追加过的链上，本方法丢弃旧列表只留入参。
     *
     * @param sheets Sheet 填充内容列表（替换后链上的完整列表）
     * @return 携带新 Sheet 列表的新 PdfTemplateChain
     * @throws NullPointerException sheets 或其元素为 null 时抛出
     */
    PdfTemplateChain sheets(List<? extends SheetData<?, ?>> sheets);

    /**
     * 设置导出 PDF 文档名的前缀（语义见 {@link PdfChain#fileNamePrefix(String)}）。
     *
     * <p>本方法是 {@link PdfChain} 上同名方法的协变重声明：返回
     * {@link PdfTemplateChain} 而非 {@link PdfChain}——先设置命名前缀、再追加或
     * 整体替换 Sheet 依然合法，填充能力不因命名配置而丢失。
     *
     * @param prefix PDF 文档名前缀
     * @return 携带新前缀的新 PdfTemplateChain
     * @throws IllegalArgumentException prefix 为 null 或空白时抛出
     */
    @Override
    PdfTemplateChain fileNamePrefix(String prefix);
}
