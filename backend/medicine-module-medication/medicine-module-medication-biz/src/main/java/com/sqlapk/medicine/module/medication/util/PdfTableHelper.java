package com.sqlapk.medicine.module.medication.util;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

/**
 * PDF表格工具类 - 解决iText方法签名不匹配问题
 * 
 * @author sqlapk
 */
public class PdfTableHelper {

    /**
     * 添加表格行 - 双参数版本（标签-值对）
     */
    public static void addTableRow(PdfPTable table, String label, String value, Font normalFont) {
        table.addCell(new PdfPCell(new Phrase(label, normalFont)));
        table.addCell(new PdfPCell(new Phrase(value != null ? value : "", normalFont)));
    }

    /**
     * 添加表格行 - 单参数版本（仅值）
     */
    public static void addTableRow(PdfPTable table, String value, Font normalFont) {
        table.addCell(new PdfPCell(new Phrase(value != null ? value : "", normalFont)));
    }

    /**
     * 添加表格表头
     */
    public static void addTableHeader(PdfPTable table, String header, Font subheaderFont) {
        PdfPCell cell = new PdfPCell(new Phrase(header, subheaderFont));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    /**
     * 添加表格行 - 多列版本
     */
    public static void addTableRow(PdfPTable table, String... values) {
        for (String value : values) {
            table.addCell(new PdfPCell(new Phrase(value != null ? value : "", 
                new Font(Font.FontFamily.HELVETICA, 12))));
        }
    }

    /**
     * 添加表格行 - 多列版本（带字体）
     */
    public static void addTableRow(PdfPTable table, Font font, String... values) {
        for (String value : values) {
            table.addCell(new PdfPCell(new Phrase(value != null ? value : "", font)));
        }
    }
}
