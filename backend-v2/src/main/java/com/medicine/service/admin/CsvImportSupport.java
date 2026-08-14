package com.medicine.service.admin;

import com.medicine.api.dto.common.CsvImportResultDTO;
import com.medicine.common.exception.BusinessException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CsvImportSupport {

    private static final long MAX_SIZE_BYTES = 1024L * 1024L;
    private static final int MAX_ROWS = 500;

    public List<CSVRecord> parse(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "CSV_FILE_REQUIRED");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BusinessException(400, "CSV_FILE_TOO_LARGE");
        }
        try {
            CSVParser parser = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build()
                    .parse(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            List<CSVRecord> rows = parser.getRecords();
            if (rows.size() > MAX_ROWS) {
                throw new BusinessException(400, "CSV_TOO_MANY_ROWS");
            }
            return rows;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(400, "CSV_PARSE_FAILED");
        }
    }

    public String value(CSVRecord row, String header) {
        if (!row.isMapped(header)) {
            return "";
        }
        String value = row.get(header);
        return value == null ? "" : value.replace("\uFEFF", "").trim();
    }

    public String required(CSVRecord row, String header, List<String> errors) {
        String value = value(row, header);
        if (!StringUtils.hasText(value)) {
            errors.add(header + " is required");
        }
        return value;
    }

    public String severity(String raw, List<String> errors) {
        String value = raw == null ? "" : raw.trim().toUpperCase();
        if (!Set.of("HIGH", "MEDIUM", "LOW").contains(value)) {
            errors.add("severity must be HIGH, MEDIUM, or LOW");
        }
        return value;
    }

    public String status(String raw) {
        return StringUtils.hasText(raw) ? raw.trim().toUpperCase() : "ACTIVE";
    }

    public void markDuplicate(String key, Set<String> seen, List<String> errors) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        if (!seen.add(key)) {
            errors.add("duplicate key in file: " + key);
        }
    }

    public CsvImportResultDTO result(List<CsvImportResultDTO.RowResult> rows) {
        CsvImportResultDTO result = new CsvImportResultDTO();
        result.setRows(rows);
        result.setTotalRows(rows.size());
        result.setCreateCount((int) rows.stream().filter(row -> "CREATE".equals(row.getAction())).count());
        result.setUpdateCount((int) rows.stream().filter(row -> "UPDATE".equals(row.getAction())).count());
        result.setErrorCount((int) rows.stream().filter(row -> row.getErrors() != null && !row.getErrors().isEmpty()).count());
        result.setWarningCount(rows.stream()
                .map(CsvImportResultDTO.RowResult::getWarnings)
                .filter(warnings -> warnings != null && !warnings.isEmpty())
                .mapToInt(List::size)
                .sum());
        return result;
    }

    public CsvImportResultDTO.RowResult rowResult(CSVRecord row, String key, String action, List<String> errors, List<String> warnings) {
        CsvImportResultDTO.RowResult rowResult = new CsvImportResultDTO.RowResult();
        rowResult.setRowNumber((int) row.getRecordNumber() + 1);
        rowResult.setKey(key);
        rowResult.setAction(errors.isEmpty() ? action : "ERROR");
        rowResult.setErrors(new ArrayList<>(errors));
        rowResult.setWarnings(new ArrayList<>(warnings));
        return rowResult;
    }

    public Set<String> newSeenSet() {
        return new HashSet<>();
    }
}
