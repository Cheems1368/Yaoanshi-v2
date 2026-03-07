package com.sqlapk.medicine.module.medication.service;

import com.sqlapk.medicine.framework.common.exception.ServiceException;
import com.sqlapk.medicine.module.medication.api.MedNotificationService;
import com.sqlapk.medicine.module.medication.api.dto.NotificationRespDTO;
import com.sqlapk.medicine.module.medication.api.dto.NotificationSendReqDTO;
import com.sqlapk.medicine.module.medication.convert.MedNotificationConvert;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedNotificationDO;
import com.sqlapk.medicine.module.medication.dal.mysql.MedNotificationMapper;
import com.sqlapk.medicine.module.medication.enums.ErrorCodeConstants;
import com.sqlapk.medicine.module.medication.enums.NotificationChannel;
import com.sqlapk.medicine.module.medication.enums.NotificationStatus;
import com.sqlapk.medicine.module.medication.enums.NotificationType;
import com.sqlapk.medicine.module.medication.enums.SendStatus;
import com.sqlapk.medicine.module.medication.service.notification.ExternalNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用药通知服务实现
 *
 * @author sqlapk
 */
@Service
@Slf4j
public class MedNotificationServiceImpl implements MedNotificationService {

    @Autowired
    private MedNotificationMapper medNotificationMapper;

    @Autowired
    private ExternalNotificationService externalNotificationService;

    @Override
    @Transactional
    public Long sendNotification(NotificationSendReqDTO sendReqDTO) {
        log.info("发送通知，患者ID: {}, 标题: {}", sendReqDTO.getPatientId(), sendReqDTO.getTitle());

        // 1. 检查免打扰设置
        if (isDoNotDisturb(sendReqDTO.getPatientId())) {
            log.info("患者 {} 处于免打扰时间，跳过通知发送", sendReqDTO.getPatientId());
            return null;
        }

        // 2. 检查是否重复通知
        if (sendReqDTO.getTodoId() != null) {
            MedNotificationDO existingNotification = medNotificationMapper.selectByTodoId(sendReqDTO.getTodoId());
            if (existingNotification != null && 
                NotificationType.MEDICATION_REMINDER.equals(NotificationType.valueOf(sendReqDTO.getType()))) {
                log.info("待办任务 {} 已有通知，跳过重复发送", sendReqDTO.getTodoId());
                return existingNotification.getId();
            }
        }

        // 3. 创建通知记录
        MedNotificationDO notification = MedNotificationDO.builder()
            .patientId(sendReqDTO.getPatientId())
            .title(sendReqDTO.getTitle())
            .content(sendReqDTO.getContent())
            .type(sendReqDTO.getType())
            .status(NotificationStatus.UNREAD.getStatus())
            .sendTime(sendReqDTO.getSendTime() != null ? sendReqDTO.getSendTime() : LocalDateTime.now())
            .todoId(sendReqDTO.getTodoId())
            .planId(sendReqDTO.getPlanId())
            .reportId(sendReqDTO.getReportId())
            .channel(sendReqDTO.getChannel())
            .sendStatus(SendStatus.PENDING.getStatus())
            .retryCount(0)
            .extInfo(sendReqDTO.getExtInfo())
            .build();

        medNotificationMapper.insert(notification);

        // 4. 异步发送通知
        try {
            sendNotificationAsync(notification);
            notification.setSendStatus(SendStatus.SUCCESS.getStatus());
            medNotificationMapper.updateById(notification);
            
            log.info("通知发送成功，通知ID: {}", notification.getId());
            return notification.getId();
            
        } catch (Exception e) {
            notification.setSendStatus(SendStatus.FAILED.getStatus());
            notification.setFailureReason(e.getMessage());
            notification.setNextRetryTime(LocalDateTime.now().plusMinutes(5)); // 5分钟后重试
            medNotificationMapper.updateById(notification);
            
            log.error("通知发送失败，通知ID: {}", notification.getId(), e);
            return notification.getId();
        }
    }

    @Override
    public List<NotificationRespDTO> getNotificationsByPatientId(Long patientId) {
        log.info("获取患者通知列表，患者ID: {}", patientId);

        List<MedNotificationDO> notifications = medNotificationMapper.selectByPatientId(patientId);
        List<NotificationRespDTO> respDTOs = MedNotificationConvert.INSTANCE.convert(notifications);

        for (NotificationRespDTO respDTO : respDTOs) {
            fillAdditionalFields(respDTO);
        }

        return respDTOs;
    }

    @Override
    public List<NotificationRespDTO> getUnreadNotificationsByPatientId(Long patientId) {
        log.info("获取患者未读通知列表，患者ID: {}", patientId);

        List<MedNotificationDO> notifications = medNotificationMapper.selectUnreadByPatientId(patientId);
        List<NotificationRespDTO> respDTOs = MedNotificationConvert.INSTANCE.convert(notifications);

        for (NotificationRespDTO respDTO : respDTOs) {
            fillAdditionalFields(respDTO);
        }

        return respDTOs;
    }

    @Override
    public List<NotificationRespDTO> getNotificationsByPatientIdAndType(Long patientId, Integer type) {
        log.info("获取患者指定类型通知列表，患者ID: {}, 类型: {}", patientId, type);

        List<MedNotificationDO> notifications = medNotificationMapper.selectByPatientIdAndType(patientId, type);
        List<NotificationRespDTO> respDTOs = MedNotificationConvert.INSTANCE.convert(notifications);

        for (NotificationRespDTO respDTO : respDTOs) {
            fillAdditionalFields(respDTO);
        }

        return respDTOs;
    }

    @Override
    @Transactional
    public void markAsRead(List<Long> notificationIds) {
        log.info("标记通知为已读，通知ID列表: {}", notificationIds);

        for (Long notificationId : notificationIds) {
            MedNotificationDO notification = medNotificationMapper.selectById(notificationId);
            if (notification != null && NotificationStatus.UNREAD.equals(NotificationStatus.valueOf(notification.getStatus()))) {
                notification.setStatus(NotificationStatus.READ.getStatus());
                notification.setReadTime(LocalDateTime.now());
                medNotificationMapper.updateById(notification);
            }
        }

        log.info("通知标记为已读完成");
    }

    @Override
    @Transactional
    public void markAllAsRead(Long patientId) {
        log.info("标记所有通知为已读，患者ID: {}", patientId);

        List<MedNotificationDO> unreadNotifications = medNotificationMapper.selectUnreadByPatientId(patientId);
        for (MedNotificationDO notification : unreadNotifications) {
            notification.setStatus(NotificationStatus.READ.getStatus());
            notification.setReadTime(LocalDateTime.now());
            medNotificationMapper.updateById(notification);
        }

        log.info("所有通知标记为已读完成");
    }

    @Override
    @Transactional
    public void deleteNotifications(List<Long> notificationIds) {
        log.info("删除通知，通知ID列表: {}", notificationIds);

        for (Long notificationId : notificationIds) {
            MedNotificationDO notification = medNotificationMapper.selectById(notificationId);
            if (notification != null) {
                notification.setStatus(NotificationStatus.DELETED.getStatus());
                medNotificationMapper.updateById(notification);
            }
        }

        log.info("通知删除完成");
    }

    @Override
    public Integer getUnreadCount(Long patientId) {
        return medNotificationMapper.countUnreadByPatientId(patientId);
    }

    @Override
    public void sendMedicationReminder(Long patientId, Long todoId, String drugName, LocalDateTime scheduledTime) {
        log.info("发送用药提醒通知，患者ID: {}, 待办ID: {}, 药品: {}", patientId, todoId, drugName);

        NotificationSendReqDTO sendReqDTO = new NotificationSendReqDTO();
        sendReqDTO.setPatientId(patientId);
        sendReqDTO.setTitle("用药提醒");
        sendReqDTO.setContent(String.format("您该服用 %s 了，计划时间：%s", drugName, scheduledTime));
        sendReqDTO.setType(NotificationType.MEDICATION_REMINDER.getType());
        sendReqDTO.setChannel(NotificationChannel.IN_APP.getChannel());
        sendReqDTO.setTodoId(todoId);

        sendNotification(sendReqDTO);
    }

    @Override
    public void sendPlanChangeNotification(Long patientId, Long planId, String changeType, String changeDescription) {
        log.info("发送计划变更通知，患者ID: {}, 计划ID: {}, 变更类型: {}", patientId, planId, changeType);

        NotificationSendReqDTO sendReqDTO = new NotificationSendReqDTO();
        sendReqDTO.setPatientId(patientId);
        sendReqDTO.setTitle("用药计划变更");
        sendReqDTO.setContent(String.format("您的用药计划已%s：%s", changeType, changeDescription));
        sendReqDTO.setType(NotificationType.PLAN_CHANGE.getType());
        sendReqDTO.setChannel(NotificationChannel.IN_APP.getChannel());
        sendReqDTO.setPlanId(planId);

        sendNotification(sendReqDTO);
    }

    @Override
    public void sendAbnormalAlert(Long patientId, String alertType, String alertMessage) {
        log.info("发送异常提醒通知，患者ID: {}, 异常类型: {}", patientId, alertType);

        NotificationSendReqDTO sendReqDTO = new NotificationSendReqDTO();
        sendReqDTO.setPatientId(patientId);
        sendReqDTO.setTitle("异常提醒");
        sendReqDTO.setContent(String.format("检测到%s：%s", alertType, alertMessage));
        sendReqDTO.setType(NotificationType.ABNORMAL_ALERT.getType());
        sendReqDTO.setChannel(NotificationChannel.IN_APP.getChannel());

        sendNotification(sendReqDTO);
    }

    @Override
    public void sendReportGeneratedNotification(Long patientId, Long reportId, String reportType) {
        log.info("发送报告生成通知，患者ID: {}, 报告ID: {}, 报告类型: {}", patientId, reportId, reportType);

        NotificationSendReqDTO sendReqDTO = new NotificationSendReqDTO();
        sendReqDTO.setPatientId(patientId);
        sendReqDTO.setTitle("报告生成完成");
        sendReqDTO.setContent(String.format("您的%s已生成完成，请查看", reportType));
        sendReqDTO.setType(NotificationType.REPORT_GENERATED.getType());
        sendReqDTO.setChannel(NotificationChannel.IN_APP.getChannel());
        sendReqDTO.setReportId(reportId);

        sendNotification(sendReqDTO);
    }

    @Override
    @Transactional
    public void batchSendNotifications(List<Long> patientIds, String title, String content, Integer type) {
        log.info("批量发送通知，患者数量: {}, 标题: {}", patientIds.size(), title);

        for (Long patientId : patientIds) {
            try {
                NotificationSendReqDTO sendReqDTO = new NotificationSendReqDTO();
                sendReqDTO.setPatientId(patientId);
                sendReqDTO.setTitle(title);
                sendReqDTO.setContent(content);
                sendReqDTO.setType(type);
                sendReqDTO.setChannel(NotificationChannel.IN_APP.getChannel());

                sendNotification(sendReqDTO);
            } catch (Exception e) {
                log.error("发送患者 {} 的通知失败", patientId, e);
            }
        }

        log.info("批量发送通知完成");
    }

    @Override
    @Transactional
    public void retrySendNotification(Long notificationId) {
        log.info("重试发送通知，通知ID: {}", notificationId);

        MedNotificationDO notification = medNotificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new ServiceException(ErrorCodeConstants.NOTIFICATION_NOT_EXISTS);
        }

        if (!SendStatus.FAILED.equals(SendStatus.valueOf(notification.getSendStatus()))) {
            throw new ServiceException(ErrorCodeConstants.NOTIFICATION_SEND_FAILED);
        }

        // 更新重试次数
        notification.setRetryCount(notification.getRetryCount() + 1);
        notification.setSendStatus(SendStatus.SENDING.getStatus());

        try {
            sendNotificationAsync(notification);
            notification.setSendStatus(SendStatus.SUCCESS.getStatus());
            notification.setFailureReason(null);
            medNotificationMapper.updateById(notification);
            
            log.info("通知重试发送成功，通知ID: {}", notificationId);
            
        } catch (Exception e) {
            notification.setSendStatus(SendStatus.FAILED.getStatus());
            notification.setFailureReason(e.getMessage());
            
            // 计算下次重试时间（指数退避）
            int retryMinutes = (int) Math.pow(2, notification.getRetryCount()) * 5;
            notification.setNextRetryTime(LocalDateTime.now().plusMinutes(retryMinutes));
            
            medNotificationMapper.updateById(notification);
            
            log.error("通知重试发送失败，通知ID: {}", notificationId, e);
        }
    }

    @Override
    @Transactional
    public void processRetryQueue() {
        log.info("处理重试队列");

        List<MedNotificationDO> retryNotifications = medNotificationMapper.selectRetryNotifications(3, LocalDateTime.now());
        
        for (MedNotificationDO notification : retryNotifications) {
            try {
                retrySendNotification(notification.getId());
            } catch (Exception e) {
                log.error("处理重试通知失败，通知ID: {}", notification.getId(), e);
            }
        }

        log.info("重试队列处理完成，处理数量: {}", retryNotifications.size());
    }

    @Override
    public boolean isDoNotDisturb(Long patientId) {
        // 这里应该从用户设置中获取免打扰配置
        // 暂时实现简单的免打扰逻辑：晚上22:00-早上8:00为免打扰时间
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        
        // 晚上22:00到早上8:00为免打扰时间
        if (hour >= 22 || hour < 8) {
            log.debug("患者 {} 当前处于免打扰时间（{}:{}）", patientId, hour, now.getMinute());
            return true;
        }
        
        return false;
    }

    @Override
    public void setDoNotDisturb(Long patientId, Boolean enabled, String startTime, String endTime) {
        log.info("设置免打扰，患者ID: {}, 启用: {}, 时间: {} - {}", patientId, enabled, startTime, endTime);
        
        // 这里应该保存到用户设置表
        // 暂时只记录日志
    }

    /**
     * 异步发送通知
     */
    private void sendNotificationAsync(MedNotificationDO notification) {
        log.debug("开始发送通知，通知ID: {}, 渠道: {}", notification.getId(), notification.getChannel());
        
        NotificationChannel channel = NotificationChannel.valueOf(notification.getChannel());
        if (channel == null) {
            throw new ServiceException(ErrorCodeConstants.NOTIFICATION_CHANNEL_INVALID);
        }
        
        try {
            switch (channel) {
                case IN_APP:
                    // 站内通知直接成功，无需外部调用
                    log.debug("站内通知发送成功，通知ID: {}", notification.getId());
                    break;
                    
                case SMS:
                    // 调用短信服务
                    sendSMSNotification(notification);
                    break;
                    
                case VOICE:
                    // 调用语音服务
                    sendVoiceNotification(notification);
                    break;
                    
                case EMAIL:
                    // 调用邮件服务
                    sendEmailNotification(notification);
                    break;
                    
                case MULTI_CHANNEL:
                    // 多渠道发送
                    sendMultiChannelNotification(notification);
                    break;
                    
                default:
                    throw new ServiceException(ErrorCodeConstants.NOTIFICATION_CHANNEL_NOT_SUPPORTED);
            }
            
        } catch (Exception e) {
            log.error("发送通知失败，通知ID: {}, 渠道: {}", notification.getId(), channel.getName(), e);
            throw e;
        }
    }

    /**
     * 发送短信通知
     */
    private void sendSMSNotification(MedNotificationDO notification) {
        // 这里需要获取患者的手机号，暂时使用模拟数据
        String phoneNumber = "13800138000"; // 实际应该从患者信息中获取
        
        boolean success = externalNotificationService.sendSMS(phoneNumber, notification.getContent());
        if (!success) {
            throw new RuntimeException("短信发送失败");
        }
        
        log.info("短信通知发送成功，通知ID: {}", notification.getId());
    }

    /**
     * 发送语音通知
     */
    private void sendVoiceNotification(MedNotificationDO notification) {
        // 这里需要获取患者的手机号，暂时使用模拟数据
        String phoneNumber = "13800138000"; // 实际应该从患者信息中获取
        
        boolean success = externalNotificationService.sendVoice(phoneNumber, notification.getContent());
        if (!success) {
            throw new RuntimeException("语音发送失败");
        }
        
        log.info("语音通知发送成功，通知ID: {}", notification.getId());
    }

    /**
     * 发送邮件通知
     */
    private void sendEmailNotification(MedNotificationDO notification) {
        // 这里需要获取患者的邮箱，暂时使用模拟数据
        String email = "patient@example.com"; // 实际应该从患者信息中获取
        
        boolean success = externalNotificationService.sendEmail(email, notification.getTitle(), notification.getContent());
        if (!success) {
            throw new RuntimeException("邮件发送失败");
        }
        
        log.info("邮件通知发送成功，通知ID: {}", notification.getId());
    }

    /**
     * 发送多渠道通知
     */
    private void sendMultiChannelNotification(MedNotificationDO notification) {
        // 这里需要获取患者的联系方式，暂时使用模拟数据
        String phoneNumber = "13800138000";
        String email = "patient@example.com";
        String openId = "wx_openid_123"; // 微信OpenID
        
        ExternalNotificationService.NotificationResult result = 
            externalNotificationService.sendMultiChannelNotification(
                phoneNumber, email, openId, 
                notification.getContent(), 
                notification.getTitle(), 
                notification.getExtInfo()
            );
        
        // 检查是否至少有一个渠道发送成功
        if (!result.isSmsSent() && !result.isEmailSent() && !result.isWechatSent() && !result.isVoiceSent()) {
            throw new RuntimeException("所有渠道发送都失败");
        }
        
        log.info("多渠道通知发送完成，通知ID: {}, SMS: {}, Email: {}, Wechat: {}, Voice: {}", 
                notification.getId(), result.isSmsSent(), result.isEmailSent(), 
                result.isWechatSent(), result.isVoiceSent());
    }

    /**
     * 填充额外字段
     */
    private void fillAdditionalFields(NotificationRespDTO respDTO) {
        // 填充类型名称
        NotificationType type = NotificationType.valueOf(respDTO.getType());
        respDTO.setTypeName(type != null ? type.getName() : "未知");

        // 填充状态名称
        NotificationStatus status = NotificationStatus.valueOf(respDTO.getStatus());
        respDTO.setStatusName(status != null ? status.getName() : "未知");

        // 填充渠道名称
        NotificationChannel channel = NotificationChannel.valueOf(respDTO.getChannel());
        respDTO.setChannelName(channel != null ? channel.getName() : "未知");

        // 填充发送状态名称
        SendStatus sendStatus = SendStatus.valueOf(respDTO.getSendStatus());
        respDTO.setSendStatusName(sendStatus != null ? sendStatus.getName() : "未知");
    }

}
