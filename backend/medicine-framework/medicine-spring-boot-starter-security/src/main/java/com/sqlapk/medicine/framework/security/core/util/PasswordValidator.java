package com.sqlapk.medicine.framework.security.core.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 密码验证器
 * 用于验证密码强度，确保密码符合安全要求
 */
@Slf4j
@Component
public class PasswordValidator {

    /**
     * 密码最小长度
     */
    private static final int MIN_LENGTH = 12;

    /**
     * 密码最大长度
     */
    private static final int MAX_LENGTH = 128;

    /**
     * 常见弱密码模式
     */
    private static final String[] WEAK_PASSWORDS = {
        "123456", "password", "admin", "admin123", "123456789",
        "qwerty", "abc123", "password123", "admin123456",
        "medicine", "pharmacy", "doctor", "patient"
    };

    /**
     * 验证密码强度
     *
     * @param password 待验证的密码
     * @return 验证结果
     */
    public PasswordValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return PasswordValidationResult.failure("密码不能为空");
        }

        // 长度检查
        if (password.length() < MIN_LENGTH) {
            return PasswordValidationResult.failure(
                String.format("密码长度不能少于%d位", MIN_LENGTH));
        }

        if (password.length() > MAX_LENGTH) {
            return PasswordValidationResult.failure(
                String.format("密码长度不能超过%d位", MAX_LENGTH));
        }

        // 弱密码检查
        if (isWeakPassword(password)) {
            return PasswordValidationResult.failure("密码过于简单，请使用更复杂的密码");
        }

        // 复杂度检查
        StringBuilder complexityIssues = new StringBuilder();
        
        if (!hasUppercase(password)) {
            complexityIssues.append("缺少大写字母; ");
        }
        
        if (!hasLowercase(password)) {
            complexityIssues.append("缺少小写字母; ");
        }
        
        if (!hasDigit(password)) {
            complexityIssues.append("缺少数字; ");
        }
        
        if (!hasSpecialChar(password)) {
            complexityIssues.append("缺少特殊字符; ");
        }

        if (complexityIssues.length() > 0) {
            return PasswordValidationResult.failure(
                "密码复杂度不足: " + complexityIssues.toString());
        }

        // 连续字符检查
        if (hasConsecutiveChars(password)) {
            return PasswordValidationResult.failure("密码不能包含连续字符");
        }

        // 重复字符检查
        if (hasRepeatedChars(password)) {
            return PasswordValidationResult.failure("密码不能包含重复字符");
        }

        return PasswordValidationResult.success("密码强度符合要求");
    }

    /**
     * 检查是否为弱密码
     */
    private boolean isWeakPassword(String password) {
        String lowerPassword = password.toLowerCase();
        for (String weakPassword : WEAK_PASSWORDS) {
            if (lowerPassword.contains(weakPassword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否包含大写字母
     */
    private boolean hasUppercase(String password) {
        return password.chars().anyMatch(Character::isUpperCase);
    }

    /**
     * 检查是否包含小写字母
     */
    private boolean hasLowercase(String password) {
        return password.chars().anyMatch(Character::isLowerCase);
    }

    /**
     * 检查是否包含数字
     */
    private boolean hasDigit(String password) {
        return password.chars().anyMatch(Character::isDigit);
    }

    /**
     * 检查是否包含特殊字符
     */
    private boolean hasSpecialChar(String password) {
        Pattern specialCharPattern = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]");
        return specialCharPattern.matcher(password).find();
    }

    /**
     * 检查是否包含连续字符
     */
    private boolean hasConsecutiveChars(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);
            
            if (c2 == c1 + 1 && c3 == c2 + 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否包含重复字符
     */
    private boolean hasRepeatedChars(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            char c = password.charAt(i);
            if (password.charAt(i + 1) == c && password.charAt(i + 2) == c) {
                return true;
            }
        }
        return false;
    }

    /**
     * 密码验证结果
     */
    public static class PasswordValidationResult {
        private final boolean valid;
        private final String message;

        private PasswordValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static PasswordValidationResult success(String message) {
            return new PasswordValidationResult(true, message);
        }

        public static PasswordValidationResult failure(String message) {
            return new PasswordValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return String.format("PasswordValidationResult{valid=%s, message='%s'}", valid, message);
        }
    }
}
