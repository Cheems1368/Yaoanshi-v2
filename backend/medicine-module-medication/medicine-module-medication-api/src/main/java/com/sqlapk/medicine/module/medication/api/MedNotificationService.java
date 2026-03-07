package com.sqlapk.medicine.module.medication.api;

import com.sqlapk.medicine.module.medication.api.dto.NotificationRespDTO;
import com.sqlapk.medicine.module.medication.api.dto.NotificationSendReqDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用药通知服务 API
 *
 * @author sqlapk
 */
public interface MedNotificationService {

    /**
     * 发送通知
     *
     * @param sendReqDTO 发送请求
     * @return 通知ID
     */
    Long sendNotification(NotificationSendReqDTO sendReqDTO);

    /**
     * 获取患者通知列表
     *
     * @param patientId 患者ID
     * @return 通知列表
     */
    List<NotificationRespDTO> getNotificationsByPatientId(Long patientId);

    /**
     * 获取患者未读通知列表
     *
     * @param patientId 患者ID
     * @return 未读通知列表
     */
    List<NotificationRespDTO> getUnreadNotificationsByPatientId(Long patientId);

    /**
     * 获取患者指定类型的通知列表
     *
     * @param patientId 患者ID
     * @param type 通知类型
     * @return 通知列表
     */
    List<NotificationRespDTO> getNotificationsByPatientIdAndType(Long patientId, Integer type);

    /**
     * 标记通知为已读
     *
     * @param notificationIds 通知ID列表
     */
    void markAsRead(List<Long> notificationIds);

    /**
     * 标记所有通知为已读
     *
     * @param patientId 患者ID
     */
    void markAllAsRead(Long patientId);

    /**
     * 删除通知
     *
     * @param notificationIds 通知ID列表
     */
    void deleteNotifications(List<Long> notificationIds);

    /**
     * 获取未读通知数量
     *
     * @param patientId 患者ID
     * @return 未读数量
     */
    Integer getUnreadCount(Long patientId);

    /**
     * 发送用药提醒通知
     *
     * @param patientId 患者ID
     * @param todoId 待办任务ID
     * @param drugName 药品名称
     * @param scheduledTime 计划时间
     */
    void sendMedicationReminder(Long patientId, Long todoId, String drugName, LocalDateTime scheduledTime);

    /**
     * 发送计划变更通知
     *
     * @param patientId 患者ID
     * @param planId 计划ID
     * @param changeType 变更类型
     * @param changeDescription 变更描述
     */
    void sendPlanChangeNotification(Long patientId, Long planId, String changeType, String changeDescription);

    /**
     * 发送异常提醒通知
     *
     * @param patientId 患者ID
     * @param alertType 异常类型
     * @param alertMessage 异常消息
     */
    void sendAbnormalAlert(Long patientId, String alertType, String alertMessage);

    /**
     * 发送报告生成通知
     *
     * @param patientId 患者ID
     * @param reportId 报告ID
     * @param reportType 报告类型
     */
    void sendReportGeneratedNotification(Long patientId, Long reportId, String reportType);

    /**
     * 批量发送通知
     *
     * @param patientIds 患者ID列表
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     */
    void batchSendNotifications(List<Long> patientIds, String title, String content, Integer type);

    /**
     * 重试发送失败的通知
     *
     * @param notificationId 通知ID
     */
    void retrySendNotification(Long notificationId);

    /**
     * 处理重试队列
     */
    void processRetryQueue();

    /**
     * 检查免打扰设置
     *
     * @param patientId 患者ID
     * @return 是否免打扰
     */
    boolean isDoNotDisturb(Long patientId);

    /**
     * 设置免打扰
     *
     * @param patientId 患者ID
     * @param enabled 是否启用
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    void setDoNotDisturb(Long patientId, Boolean enabled, String startTime, String endTime);

}
