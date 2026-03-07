package com.sqlapk.medicine.module.patient.service.patient.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 患者档案验证结果
 */
@Data
public class ValidationResult {

    /**
     * 验证是否通过
     */
    private boolean valid = true;

    /**
     * 错误信息列表
     */
    private List<String> errors = new ArrayList<>();

    /**
     * 警告信息列表
     */
    private List<String> warnings = new ArrayList<>();

    /**
     * 添加错误信息
     */
    public void addError(String error) {
        this.errors.add(error);
        this.valid = false;
    }

    /**
     * 添加警告信息
     */
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    /**
     * 检查是否有错误
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * 检查是否有警告
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * 获取所有消息
     */
    public List<String> getAllMessages() {
        List<String> messages = new ArrayList<>();
        messages.addAll(errors);
        messages.addAll(warnings);
        return messages;
    }

    /**
     * 获取错误数量
     */
    public int getErrorCount() {
        return errors.size();
    }

    /**
     * 获取警告数量
     */
    public int getWarningCount() {
        return warnings.size();
    }

}
