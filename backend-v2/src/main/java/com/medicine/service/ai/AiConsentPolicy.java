package com.medicine.service.ai;

import com.medicine.config.AiProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class AiConsentPolicy {

    private final AiProperties properties;

    public AiConsentPolicy(AiProperties properties) {
        this.properties = properties;
    }

    public String version() {
        return properties.getPolicyVersion();
    }

    public String providerCode() {
        return properties.getProviderCode();
    }

    public String providerDisplayName() {
        return properties.getProviderDisplayName();
    }

    public String statement() {
        return "本功能会将您输入的最近对话发送至第三方 AI 服务“"
                + providerDisplayName()
                + "”处理。发送前，系统会自动隐藏手机号、身份证号、邮箱以及明确标注的姓名和就诊编号；"
                + "不会自动拼接您的账户手机号、证件号、患者 ID 或健康档案。"
                + "系统仅保存供应商、模型、消息数量、脱敏数量、调用结果等审计元数据，不保存您的问题或 AI 回复正文；"
                + "审计元数据保留 " + properties.getAuditRetentionDays() + " 天。"
                + "请勿主动输入不必要的身份信息。AI 回复仅供用药科普参考，不能替代医生面诊或医疗决策。";
    }

    public String statementHash() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(statement().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
