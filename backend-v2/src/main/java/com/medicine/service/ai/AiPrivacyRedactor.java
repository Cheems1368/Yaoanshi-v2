package com.medicine.service.ai;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AiPrivacyRedactor {

    private static final List<Rule> RULES = List.of(
            new Rule(Pattern.compile("(?i)(?<![A-Z0-9._%+-])[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}(?![A-Z0-9._%+-])"), "[邮箱已隐藏]"),
            new Rule(Pattern.compile("(?<!\\d)(?:\\d{17}[0-9Xx]|\\d{15})(?!\\d)"), "[身份证号已隐藏]"),
            new Rule(Pattern.compile("(?<!\\d)1[3-9]\\d{9}(?!\\d)"), "[手机号已隐藏]"),
            new Rule(Pattern.compile("(患者姓名|姓名)\\s*[:：]\\s*[\\p{IsHan}A-Za-z·.]{2,32}"), "$1：[姓名已隐藏]"),
            new Rule(Pattern.compile("(?i)(患者号|患者\\s*ID|就诊编号|就诊号|就诊\\s*ID|住院号|门诊号|病历号)\\s*[:：]\\s*[A-Za-z0-9._-]{2,32}"), "$1：[编号已隐藏]")
    );

    public RedactionResult redact(String input) {
        String value = input == null ? "" : input;
        int count = 0;
        for (Rule rule : RULES) {
            Matcher matcher = rule.pattern.matcher(value);
            StringBuffer buffer = new StringBuffer();
            while (matcher.find()) {
                count++;
                matcher.appendReplacement(buffer, rule.replacement);
            }
            matcher.appendTail(buffer);
            value = buffer.toString();
        }
        return new RedactionResult(value, count);
    }

    private record Rule(Pattern pattern, String replacement) {
    }

    public record RedactionResult(String text, int redactionCount) {
    }
}
