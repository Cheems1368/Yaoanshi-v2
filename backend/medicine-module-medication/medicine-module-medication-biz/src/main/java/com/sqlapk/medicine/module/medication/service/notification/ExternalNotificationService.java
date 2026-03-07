package com.sqlapk.medicine.module.medication.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 外部通知通道服务
 */
@Slf4j
@Service
public class ExternalNotificationService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    // 短信服务配置
    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${notification.sms.api-url:}")
    private String smsApiUrl;

    @Value("${notification.sms.api-key:}")
    private String smsApiKey;

    @Value("${notification.sms.secret-key:}")
    private String smsSecretKey;

    // 邮件服务配置
    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${notification.email.smtp-host:}")
    private String smtpHost;

    @Value("${notification.email.smtp-port:587}")
    private int smtpPort;

    @Value("${notification.email.username:}")
    private String emailUsername;

    @Value("${notification.email.password:}")
    private String emailPassword;

    @Value("${notification.email.from:}")
    private String emailFrom;

    // 语音服务配置
    @Value("${notification.voice.enabled:false}")
    private boolean voiceEnabled;

    @Value("${notification.voice.api-url:}")
    private String voiceApiUrl;

    @Value("${notification.voice.api-key:}")
    private String voiceApiKey;

    // 微信服务配置
    @Value("${notification.wechat.enabled:false}")
    private boolean wechatEnabled;

    @Value("${notification.wechat.app-id:}")
    private String wechatAppId;

    @Value("${notification.wechat.app-secret:}")
    private String wechatAppSecret;

    @Value("${notification.wechat.template-id:}")
    private String wechatTemplateId;

    /**
     * 发送短信通知
     */
    public boolean sendSMS(String phoneNumber, String message) {
        if (!smsEnabled) {
            log.warn("短信服务未启用");
            return false;
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("phone", phoneNumber);
            requestBody.put("message", message);
            requestBody.put("apiKey", smsApiKey);
            requestBody.put("secretKey", smsSecretKey);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(smsApiUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("短信发送成功: {}", phoneNumber);
                return true;
            } else {
                log.error("短信发送失败: {}, 响应: {}", phoneNumber, response.getBody());
                return false;
            }
        } catch (Exception e) {
            log.error("短信发送异常: {}, 错误: {}", phoneNumber, e.getMessage());
            return false;
        }
    }

    /**
     * 发送邮件通知
     */
    public boolean sendEmail(String toEmail, String subject, String content) {
        if (!emailEnabled) {
            log.warn("邮件服务未启用");
            return false;
        }

        try {
            // 优先使用JavaMailSender
            if (javaMailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(emailFrom);
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(content);
                
                javaMailSender.send(message);
                log.info("邮件发送成功 (JavaMailSender): {}", toEmail);
                return true;
            } else {
                // 备用方案：使用HTTP API
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("to", toEmail);
                requestBody.put("subject", subject);
                requestBody.put("content", content);
                requestBody.put("from", emailFrom);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
                
                // 假设有一个邮件服务API
                String emailApiUrl = "https://api.email-service.com/send";
                ResponseEntity<Map> response = restTemplate.postForEntity(emailApiUrl, request, Map.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("邮件发送成功 (HTTP API): {}", toEmail);
                    return true;
                } else {
                    log.error("邮件发送失败: {}, 响应: {}", toEmail, response.getBody());
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("邮件发送异常: {}, 错误: {}", toEmail, e.getMessage());
            return false;
        }
    }

    /**
     * 发送语音通知
     */
    public boolean sendVoice(String phoneNumber, String message) {
        if (!voiceEnabled) {
            log.warn("语音服务未启用");
            return false;
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("phone", phoneNumber);
            requestBody.put("message", message);
            requestBody.put("apiKey", voiceApiKey);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(voiceApiUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("语音发送成功: {}", phoneNumber);
                return true;
            } else {
                log.error("语音发送失败: {}, 响应: {}", phoneNumber, response.getBody());
                return false;
            }
        } catch (Exception e) {
            log.error("语音发送异常: {}, 错误: {}", phoneNumber, e.getMessage());
            return false;
        }
    }

    /**
     * 发送微信通知
     */
    public boolean sendWechat(String openId, String templateData) {
        if (!wechatEnabled) {
            log.warn("微信服务未启用");
            return false;
        }

        try {
            // 获取访问令牌
            String accessToken = getWechatAccessToken();
            if (accessToken == null) {
                log.error("获取微信访问令牌失败");
                return false;
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("touser", openId);
            requestBody.put("template_id", wechatTemplateId);
            requestBody.put("data", templateData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            String wechatApiUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken;
            ResponseEntity<Map> response = restTemplate.postForEntity(wechatApiUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && "0".equals(responseBody.get("errcode").toString())) {
                    log.info("微信通知发送成功: {}", openId);
                    return true;
                } else {
                    log.error("微信通知发送失败: {}, 响应: {}", openId, responseBody);
                    return false;
                }
            } else {
                log.error("微信通知发送失败: {}, 响应: {}", openId, response.getBody());
                return false;
            }
        } catch (Exception e) {
            log.error("微信通知发送异常: {}, 错误: {}", openId, e.getMessage());
            return false;
        }
    }

    /**
     * 获取微信访问令牌
     */
    private String getWechatAccessToken() {
        try {
            String url = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                    wechatAppId, wechatAppSecret);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("access_token")) {
                    return responseBody.get("access_token").toString();
                }
            }
            
            log.error("获取微信访问令牌失败: {}", response.getBody());
            return null;
        } catch (Exception e) {
            log.error("获取微信访问令牌异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 发送多渠道通知
     */
    public NotificationResult sendMultiChannelNotification(String phoneNumber, String email, String openId, 
                                                          String message, String subject, String templateData) {
        NotificationResult result = new NotificationResult();
        
        // 发送短信
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            result.setSmsSent(sendSMS(phoneNumber, message));
        }
        
        // 发送邮件
        if (email != null && !email.isEmpty()) {
            result.setEmailSent(sendEmail(email, subject, message));
        }
        
        // 发送微信
        if (openId != null && !openId.isEmpty()) {
            result.setWechatSent(sendWechat(openId, templateData));
        }
        
        // 发送语音（作为备用）
        if (phoneNumber != null && !phoneNumber.isEmpty() && !result.isSmsSent()) {
            result.setVoiceSent(sendVoice(phoneNumber, message));
        }
        
        return result;
    }

    /**
     * 通知结果
     */
    public static class NotificationResult {
        private boolean smsSent = false;
        private boolean emailSent = false;
        private boolean voiceSent = false;
        private boolean wechatSent = false;

        // Getters and Setters
        public boolean isSmsSent() { return smsSent; }
        public void setSmsSent(boolean smsSent) { this.smsSent = smsSent; }

        public boolean isEmailSent() { return emailSent; }
        public void setEmailSent(boolean emailSent) { this.emailSent = emailSent; }

        public boolean isVoiceSent() { return voiceSent; }
        public void setVoiceSent(boolean voiceSent) { this.voiceSent = voiceSent; }

        public boolean isWechatSent() { return wechatSent; }
        public void setWechatSent(boolean wechatSent) { this.wechatSent = wechatSent; }

        public boolean isAnySent() {
            return smsSent || emailSent || voiceSent || wechatSent;
        }

        public int getSuccessCount() {
            int count = 0;
            if (smsSent) count++;
            if (emailSent) count++;
            if (voiceSent) count++;
            if (wechatSent) count++;
            return count;
        }
    }
}
