package com.medicine.service.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class PrescriptionOcrParser {

    private static final double MIN_SCORE = 0.90;
    private static final Pattern EXCLUDED = Pattern.compile(
            "患者|姓名|性别|年龄|身份证|手机|电话|地址|编号|病历|就诊|诊断|医生|医师|药师|签名|日期|说明|备注|\\d{11,}");
    private static final Pattern FREQUENCY = Pattern.compile(
            "(?:每日|每天|每晚|每晨)[一二两三四五六七八九十\\d]+次|每\\d+小时[一二两\\d]+次|\\b(?:QD|BID|TID|QID|Q\\d+H)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern ROUTE = Pattern.compile("静脉滴注|皮下注射|肌内注射|口服|外用|静滴");
    private static final Pattern DOSE = Pattern.compile("每次\\s*([0-9]+(?:\\.[0-9]+)?\\s*(?:mg|g|ml|mL|片|粒|袋|滴|支))");
    private static final Pattern LABEL = Pattern.compile(
            "(药品名称|药名|规格|单次剂量|每次用量|剂量|频次|用法|给药途径|途径|疗程)[：:]");
    private final ObjectMapper objectMapper;

    public PrescriptionOcrParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public record TextBlock(String text, double score, double[][] box) {
        public boolean valid() {
            return text != null && Double.isFinite(score) && score >= 0 && score <= 1 && box != null
                    && box.length == 4 && Arrays.stream(box).allMatch(p -> p != null && p.length == 2
                    && Double.isFinite(p[0]) && Double.isFinite(p[1]) && p[0] >= 0 && p[1] >= 0)
                    && right() > left() && bottom() > top();
        }
        double left() { return Arrays.stream(box).mapToDouble(p -> p[0]).min().orElse(0); }
        double right() { return Arrays.stream(box).mapToDouble(p -> p[0]).max().orElse(0); }
        double top() { return Arrays.stream(box).mapToDouble(p -> p[1]).min().orElse(0); }
        double bottom() { return Arrays.stream(box).mapToDouble(p -> p[1]).max().orElse(0); }
        double x() { return (left() + right()) / 2; }
        double y() { return (top() + bottom()) / 2; }
        double height() { return bottom() - top(); }
    }

    private record Column(String field, TextBlock header) { }

    public OcrService.OcrResult parse(List<TextBlock> blocks) {
        if (blocks.isEmpty()) {
            return OcrService.OcrResult.failure("OCR_NO_TEXT");
        }
        if (blocks.stream().anyMatch(b -> !b.valid())) {
            return OcrService.OcrResult.failure("OCR_RESPONSE_INVALID");
        }
        List<List<TextBlock>> rows = rows(blocks);
        List<OcrService.OcrItem> items = new ArrayList<>();
        List<String> medicationLines = new ArrayList<>();
        for (int index = 0; index < rows.size(); index++) {
            List<Column> columns = rows.get(index).stream().filter(b -> b.score() >= MIN_SCORE)
                    .filter(b -> field(b.text()) != null).map(b -> new Column(field(b.text()), b))
                    .sorted(Comparator.comparingDouble(c -> c.header().x())).toList();
            if (columns.size() < 2 || columns.stream().noneMatch(c -> "drugName".equals(c.field()))) {
                continue;
            }
            if (columns.stream().map(Column::field).distinct().count() != columns.size()) {
                return OcrService.OcrResult.failure("OCR_LAYOUT_AMBIGUOUS");
            }
            for (int next = index + 1; next < rows.size(); next++) {
                List<TextBlock> row = rows.get(next);
                if (row.stream().anyMatch(b -> EXCLUDED.matcher(b.text()).find() || field(b.text()) != null)) {
                    break;
                }
                Map<String, TextBlock> cells = new LinkedHashMap<>();
                boolean ambiguous = false;
                for (TextBlock block : row) {
                    int column = 0;
                    while (column + 1 < columns.size()
                            && block.left() >= columns.get(column + 1).header().left()) {
                        column++;
                    }
                    double lower = column == 0 ? Double.NEGATIVE_INFINITY
                            : columns.get(column).header().left();
                    double upper = column + 1 == columns.size() ? Double.POSITIVE_INFINITY
                            : columns.get(column + 1).header().left();
                    if (block.left() < lower || block.right() > upper
                            || cells.put(columns.get(column).field(), block) != null) {
                        ambiguous = true;
                    }
                }
                if (ambiguous || !cells.containsKey("drugName")) {
                    continue;
                }
                medicationLines.add(row.stream().map(TextBlock::text).collect(Collectors.joining(" ")));
                addItem(cells, columns.stream().mapToDouble(c -> c.header().score()).min().orElse(0), items);
            }
            return result(items, medicationLines);
        }
        // Explicit labels are accepted without guessing which unlabelled lines belong to a medicine.
        for (List<TextBlock> row : rows) {
            if (row.size() != 1 || EXCLUDED.matcher(row.get(0).text()).find()) {
                continue;
            }
            TextBlock block = row.get(0);
            Matcher matcher = LABEL.matcher(block.text());
            List<String> labels = new ArrayList<>();
            List<Integer> starts = new ArrayList<>();
            List<Integer> ends = new ArrayList<>();
            while (matcher.find()) {
                labels.add(field(matcher.group(1)));
                starts.add(matcher.start());
                ends.add(matcher.end());
            }
            Map<String, TextBlock> cells = new LinkedHashMap<>();
            for (int i = 0; i < labels.size(); i++) {
                String value = block.text().substring(ends.get(i), i + 1 < labels.size() ? starts.get(i + 1) : block.text().length())
                        .replaceAll("^[\\s；;,，]+|[\\s；;,，]+$", "");
                if (cells.put(labels.get(i), new TextBlock(value, block.score(), block.box())) != null) {
                    cells.clear();
                    break;
                }
            }
            if (cells.containsKey("drugName") && !cells.get("drugName").text().isBlank()) {
                medicationLines.add(block.text());
                addItem(cells, block.score(), items);
            }
        }
        return result(items, medicationLines);
    }

    private void addItem(Map<String, TextBlock> cells, double headerScore, List<OcrService.OcrItem> items) {
        TextBlock name = cells.get("drugName");
        if (name == null || name.score() < MIN_SCORE || name.text().isBlank()) {
            return;
        }
        OcrService.OcrItem item = new OcrService.OcrItem();
        item.setDrugName(name.text().trim());
        double confidence = Math.min(headerScore, name.score());
        for (Map.Entry<String, TextBlock> entry : cells.entrySet()) {
            TextBlock block = entry.getValue();
            if (block.score() < MIN_SCORE || block.text().isBlank()) {
                continue;
            }
            String text = block.text().trim();
            boolean used = true;
            switch (entry.getKey()) {
                case "drugName" -> { }
                case "drugSpec" -> item.setDrugSpec(text);
                case "dosage" -> item.setDosage(text);
                case "frequency" -> item.setFrequency(text);
                case "route" -> item.setRoute(text);
                case "usage" -> {
                    String frequency = match(FREQUENCY, text);
                    String route = match(ROUTE, text);
                    Matcher dose = DOSE.matcher(text);
                    String dosage = dose.find() ? dose.group(1) : null;
                    if (item.getFrequency() == null) { item.setFrequency(frequency); }
                    if (item.getRoute() == null) { item.setRoute(route); }
                    if (item.getDosage() == null) { item.setDosage(dosage); }
                    used = frequency != null || route != null || dosage != null;
                }
                case "durationDays" -> {
                    if (text.matches("[1-9][0-9]{0,2}天")) {
                        item.setDurationDays(Integer.parseInt(text.substring(0, text.length() - 1)));
                    } else { used = false; }
                }
                default -> used = false;
            }
            if (used) { confidence = Math.min(confidence, block.score()); }
        }
        item.setConfidence(BigDecimal.valueOf(confidence));
        items.add(item);
    }

    private OcrService.OcrResult result(List<OcrService.OcrItem> items, List<String> lines) {
        String text = String.join("\n", lines);
        if (items.isEmpty()) {
            return OcrService.OcrResult.reviewRequired(text.isEmpty() ? null : text,
                    lines.isEmpty() ? "OCR_MEDICATION_LAYOUT_UNSUPPORTED" : "OCR_MEDICATION_LOW_CONFIDENCE");
        }
        BigDecimal confidence = items.stream().map(OcrService.OcrItem::getConfidence).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        try {
            String safeJson = objectMapper.writeValueAsString(Map.of("rawText", text, "items", items, "overallConfidence", confidence));
            return OcrService.OcrResult.success(text, items, confidence, safeJson);
        } catch (Exception ex) {
            return OcrService.OcrResult.failure("OCR_SERIALIZATION_FAILED");
        }
    }

    private List<List<TextBlock>> rows(List<TextBlock> blocks) {
        List<List<TextBlock>> rows = new ArrayList<>();
        for (TextBlock block : blocks.stream().sorted(Comparator.comparingDouble(TextBlock::y).thenComparingDouble(TextBlock::x)).toList()) {
            List<TextBlock> last = rows.isEmpty() ? null : rows.get(rows.size() - 1);
            if (last == null || Math.abs(block.y() - last.get(0).y()) > Math.min(block.height(), last.get(0).height()) * 0.45) {
                last = new ArrayList<>();
                rows.add(last);
            }
            last.add(block);
        }
        rows.forEach(row -> row.sort(Comparator.comparingDouble(TextBlock::x)));
        return rows;
    }

    private static String match(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    private static String field(String text) {
        return switch (text.replaceAll("\\s+", "")) {
            case "药品名称", "药名" -> "drugName";
            case "规格" -> "drugSpec";
            case "剂量", "单次剂量", "每次用量" -> "dosage";
            case "频次" -> "frequency";
            case "用法" -> "usage";
            case "给药途径", "途径" -> "route";
            case "疗程" -> "durationDays";
            default -> null;
        };
    }
}
