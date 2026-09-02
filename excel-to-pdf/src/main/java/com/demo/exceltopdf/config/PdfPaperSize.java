package com.demo.exceltopdf.config;

import com.demo.exceltopdf.util.PdfUtil;
import org.apache.poi.ss.usermodel.PrintSetup;

/**
 * 转换产物 PDF 的目标纸张大小（配置项 {@code excel-to-pdf.libreoffice.paper-size}）。
 *
 * <p>LibreOffice 导出 PDF 时没有「纸张大小」参数，PDF 纸型只由源 xlsx 每个 Sheet 的
 * 页面设置决定。因此本枚举只表达「PDF 成品想要多大纸」，具体如何落到 xlsx 页面设置
 * 由 {@link PdfUtil} 在转换前改写完成。
 *
 * @author Tomatos
 */
public enum PdfPaperSize {

    /** A4（210 × 297 mm） */
    A4(PrintSetup.A4_PAPERSIZE),

    /** US Letter（8.5 × 11 英寸） */
    LETTER(PrintSetup.LETTER_PAPERSIZE);

    /** OOXML 纸张编码（即 POI {@link PrintSetup} 的纸张常量） */
    private final short ooxmlCode;

    PdfPaperSize(short ooxmlCode) {
        this.ooxmlCode = ooxmlCode;
    }

    /**
     * POI 页面设置使用的纸张编码。
     *
     * @return 可直接传给 {@link PrintSetup#setPaperSize(short)}
     */
    public short getOoxmlCode() {
        return ooxmlCode;
    }
}
