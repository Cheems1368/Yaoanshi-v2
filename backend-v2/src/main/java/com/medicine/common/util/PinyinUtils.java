package com.medicine.common.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 拼音工具：从中文字符串提取每个汉字的声母首字母，用于药品目录索引。
 * 例：阿司匹林 → aspl，氯吡格雷 → lbgl
 */
public final class PinyinUtils {

    private static final HanyuPinyinOutputFormat FORMAT;

    static {
        FORMAT = new HanyuPinyinOutputFormat();
        FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    private PinyinUtils() {}

    /**
     * 提取中文字符串的拼音首字母序列（仅取汉字，跳过非汉字字符）。
     * 多音字取第一个读音的首字母。
     */
    public static String toPinyinInitials(String chineseText) {
        if (chineseText == null || chineseText.isBlank()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : chineseText.toCharArray()) {
            if (c >= '\u4e00' && c <= '\u9fa5') {
                try {
                    String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c, FORMAT);
                    if (pinyins != null && pinyins.length > 0 && !pinyins[0].isEmpty()) {
                        sb.append(pinyins[0].charAt(0));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination ignored) {
                    // 跳过无法转换的字符
                }
            }
        }
        return sb.toString();
    }
}
