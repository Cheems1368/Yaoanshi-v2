package com.sqlapk.medicine.module.medication.controller.admin;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.framework.operatelog.core.enums.OperateTypeEnum;
import com.sqlapk.medicine.module.medication.api.MedNotificationService;
import com.sqlapk.medicine.module.medication.api.dto.NotificationRespDTO;
import com.sqlapk.medicine.module.medication.api.dto.NotificationSendReqDTO;
import com.sqlapk.medicine.module.medication.controller.admin.vo.NotificationPageReqVO;
import com.sqlapk.medicine.module.medication.controller.admin.vo.NotificationRespVO;
import com.sqlapk.medicine.module.medication.convert.MedNotificationConvert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 用药通知
 *
 * @author sqlapk
 */
@Tag(name = "管理后台 - 用药通知")
@RestController
@RequestMapping("/medication/notification")
@Validated
@Slf4j
public class MedNotificationController {

    @Autowired
    private MedNotificationService medNotificationService;

    @PostMapping("/send")
    @Operation(summary = "发送通知")
    @PreAuthorize("@ss.hasPermission('medication:notification:send')")
    @OperateLog(module = "用药打卡系统", name = "发送通知", type = OperateTypeEnum.CREATE)
    public CommonResult<Long> sendNotification(@Valid @RequestBody NotificationSendReqDTO sendReqDTO) {
        Long notificationId = medNotificationService.sendNotification(sendReqDTO);
        return success(notificationId);
    }

    @GetMapping("/list")
    @Operation(summary = "获取患者通知列表")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:notification:query')")
    public CommonResult<List<NotificationRespVO>> getNotificationsByPatientId(@RequestParam("patientId") Long patientId) {
        List<NotificationRespDTO> respDTOs = medNotificationService.getNotificationsByPatientId(patientId);
        List<NotificationRespVO> respVOs = MedNotificationConvert.INSTANCE.toVOList(respDTOs);
        return success(respVOs);
    }

    @GetMapping("/unread")
    @Operation(summary = "获取患者未读通知列表")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:notification:query')")
    public CommonResult<List<NotificationRespVO>> getUnreadNotificationsByPatientId(@RequestParam("patientId") Long patientId) {
        List<NotificationRespDTO> respDTOs = medNotificationService.getUnreadNotificationsByPatientId(patientId);
        List<NotificationRespVO> respVOs = MedNotificationConvert.INSTANCE.toVOList(respDTOs);
        return success(respVOs);
    }

    @GetMapping("/by-type")
    @Operation(summary = "获取患者指定类型通知列表")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @Parameter(name = "type", description = "通知类型", required = true)
    @PreAuthorize("@ss.hasPermission('medication:notification:query')")
    public CommonResult<List<NotificationRespVO>> getNotificationsByPatientIdAndType(
            @RequestParam("patientId") Long patientId,
            @RequestParam("type") Integer type) {
        
        List<NotificationRespDTO> respDTOs = medNotificationService.getNotificationsByPatientIdAndType(patientId, type);
        List<NotificationRespVO> respVOs = MedNotificationConvert.INSTANCE.toVOList(respDTOs);
        return success(respVOs);
    }

    @PostMapping("/mark-read")
    @Operation(summary = "标记通知为已读")
    @PreAuthorize("@ss.hasPermission('medication:notification:update')")
    @OperateLog(module = "用药打卡系统", name = "标记通知已读", type = OperateTypeEnum.UPDATE)
    public CommonResult<Boolean> markAsRead(@RequestBody MarkReadReqVO markReadReqVO) {
        medNotificationService.markAsRead(markReadReqVO.getNotificationIds());
        return success(true);
    }

    @PostMapping("/mark-all-read")
    @Operation(summary = "标记所有通知为已读")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:notification:update')")
    @OperateLog(module = "用药打卡系统", name = "标记所有通知已读", type = OperateTypeEnum.UPDATE)
    public CommonResult<Boolean> markAllAsRead(@RequestParam("patientId") Long patientId) {
        medNotificationService.markAllAsRead(patientId);
        return success(true);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除通知")
    @PreAuthorize("@ss.hasPermission('medication:notification:delete')")
    @OperateLog(module = "用药打卡系统", name = "删除通知", type = OperateTypeEnum.DELETE)
    public CommonResult<Boolean> deleteNotifications(@RequestBody DeleteReqVO deleteReqVO) {
        medNotificationService.deleteNotifications(deleteReqVO.getNotificationIds());
        return success(true);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数量")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:notification:query')")
    public CommonResult<Integer> getUnreadCount(@RequestParam("patientId") Long patientId) {
        Integer count = medNotificationService.getUnreadCount(patientId);
        return success(count);
    }

    @PostMapping("/send-medication-reminder")
    @Operation(summary = "发送用药提醒通知")
    @PreAuthorize("@ss.hasPermission('medication:notification:send')")
    @OperateLog(module = "用药打卡系统", name = "发送用药提醒", type = OperateTypeEnum.CREATE)
    public CommonResult<Boolean> sendMedicationReminder(@RequestBody MedicationReminderReqVO reminderReqVO) {
        medNotificationService.sendMedicationReminder(
            reminderReqVO.getPatientId(),
            reminderReqVO.getTodoId(),
            reminderReqVO.getDrugName(),
            reminderReqVO.getScheduledTime()
        );
        return success(true);
    }

    @PostMapping("/send-plan-change")
    @Operation(summary = "发送计划变更通知")
    @PreAuthorize("@ss.hasPermission('medication:notification:send')")
    @OperateLog(module = "用药打卡系统", name = "发送计划变更通知", type = OperateTypeEnum.CREATE)
    public CommonResult<Boolean> sendPlanChangeNotification(@RequestBody PlanChangeReqVO planChangeReqVO) {
        medNotificationService.sendPlanChangeNotification(
            planChangeReqVO.getPatientId(),
            planChangeReqVO.getPlanId(),
            planChangeReqVO.getChangeType(),
            planChangeReqVO.getChangeDescription()
        );
        return success(true);
    }

    @PostMapping("/send-abnormal-alert")
    @Operation(summary = "发送异常提醒通知")
    @PreAuthorize("@ss.hasPermission('medication:notification:send')")
    @OperateLog(module = "用药打卡系统", name = "发送异常提醒", type = OperateTypeEnum.CREATE)
    public CommonResult<Boolean> sendAbnormalAlert(@RequestBody AbnormalAlertReqVO alertReqVO) {
        medNotificationService.sendAbnormalAlert(
            alertReqVO.getPatientId(),
            alertReqVO.getAlertType(),
            alertReqVO.getAlertMessage()
        );
        return success(true);
    }

    @PostMapping("/send-report-generated")
    @Operation(summary = "发送报告生成通知")
    @PreAuthorize("@ss.hasPermission('medication:notification:send')")
    @OperateLog(module = "用药打卡系统", name = "发送报告生成通知", type = OperateTypeEnum.CREATE)
    public CommonResult<Boolean> sendReportGeneratedNotification(@RequestBody ReportGeneratedReqVO reportReqVO) {
        medNotificationService.sendReportGeneratedNotification(
            reportReqVO.getPatientId(),
            reportReqVO.getReportId(),
            reportReqVO.getReportType()
        );
        return success(true);
    }

    @PostMapping("/batch-send")
    @Operation(summary = "批量发送通知")
    @PreAuthorize("@ss.hasPermission('medication:notification:send')")
    @OperateLog(module = "用药打卡系统", name = "批量发送通知", type = OperateTypeEnum.CREATE)
    public CommonResult<Boolean> batchSendNotifications(@RequestBody BatchSendReqVO batchReqVO) {
        medNotificationService.batchSendNotifications(
            batchReqVO.getPatientIds(),
            batchReqVO.getTitle(),
            batchReqVO.getContent(),
            batchReqVO.getType()
        );
        return success(true);
    }

    @PostMapping("/retry")
    @Operation(summary = "重试发送通知")
    @Parameter(name = "notificationId", description = "通知ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:notification:send')")
    @OperateLog(module = "用药打卡系统", name = "重试发送通知", type = OperateTypeEnum.UPDATE)
    public CommonResult<Boolean> retrySendNotification(@RequestParam("notificationId") Long notificationId) {
        medNotificationService.retrySendNotification(notificationId);
        return success(true);
    }

    @PostMapping("/process-retry-queue")
    @Operation(summary = "处理重试队列")
    @PreAuthorize("@ss.hasPermission('medication:notification:send')")
    @OperateLog(module = "用药打卡系统", name = "处理重试队列", type = OperateTypeEnum.UPDATE)
    public CommonResult<Boolean> processRetryQueue() {
        medNotificationService.processRetryQueue();
        return success(true);
    }

    @GetMapping("/do-not-disturb")
    @Operation(summary = "检查免打扰设置")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:notification:query')")
    public CommonResult<Boolean> isDoNotDisturb(@RequestParam("patientId") Long patientId) {
        boolean isDoNotDisturb = medNotificationService.isDoNotDisturb(patientId);
        return success(isDoNotDisturb);
    }

    @PostMapping("/set-do-not-disturb")
    @Operation(summary = "设置免打扰")
    @PreAuthorize("@ss.hasPermission('medication:notification:update')")
    @OperateLog(module = "用药打卡系统", name = "设置免打扰", type = OperateTypeEnum.UPDATE)
    public CommonResult<Boolean> setDoNotDisturb(@RequestBody DoNotDisturbReqVO doNotDisturbReqVO) {
        medNotificationService.setDoNotDisturb(
            doNotDisturbReqVO.getPatientId(),
            doNotDisturbReqVO.getEnabled(),
            doNotDisturbReqVO.getStartTime(),
            doNotDisturbReqVO.getEndTime()
        );
        return success(true);
    }

    /**
     * 标记已读请求 VO
     */
    public static class MarkReadReqVO {
        private java.util.List<Long> notificationIds;

        public java.util.List<Long> getNotificationIds() { 
            return notificationIds != null ? new java.util.ArrayList<>(notificationIds) : null; 
        }
        public void setNotificationIds(java.util.List<Long> notificationIds) { 
            this.notificationIds = notificationIds != null ? new java.util.ArrayList<>(notificationIds) : null; 
        }
    }

    /**
     * 删除请求 VO
     */
    public static class DeleteReqVO {
        private java.util.List<Long> notificationIds;

        public java.util.List<Long> getNotificationIds() { 
            return notificationIds != null ? new java.util.ArrayList<>(notificationIds) : null; 
        }
        public void setNotificationIds(java.util.List<Long> notificationIds) { 
            this.notificationIds = notificationIds != null ? new java.util.ArrayList<>(notificationIds) : null; 
        }
    }

    /**
     * 用药提醒请求 VO
     */
    public static class MedicationReminderReqVO {
        private Long patientId;
        private Long todoId;
        private String drugName;
        private java.time.LocalDateTime scheduledTime;

        // Getters and Setters
        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }
        
        public Long getTodoId() { return todoId; }
        public void setTodoId(Long todoId) { this.todoId = todoId; }
        
        public String getDrugName() { return drugName; }
        public void setDrugName(String drugName) { this.drugName = drugName; }
        
        public java.time.LocalDateTime getScheduledTime() { return scheduledTime; }
        public void setScheduledTime(java.time.LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
    }

    /**
     * 计划变更请求 VO
     */
    public static class PlanChangeReqVO {
        private Long patientId;
        private Long planId;
        private String changeType;
        private String changeDescription;

        // Getters and Setters
        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }
        
        public Long getPlanId() { return planId; }
        public void setPlanId(Long planId) { this.planId = planId; }
        
        public String getChangeType() { return changeType; }
        public void setChangeType(String changeType) { this.changeType = changeType; }
        
        public String getChangeDescription() { return changeDescription; }
        public void setChangeDescription(String changeDescription) { this.changeDescription = changeDescription; }
    }

    /**
     * 异常提醒请求 VO
     */
    public static class AbnormalAlertReqVO {
        private Long patientId;
        private String alertType;
        private String alertMessage;

        // Getters and Setters
        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }
        
        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }
        
        public String getAlertMessage() { return alertMessage; }
        public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }
    }

    /**
     * 报告生成请求 VO
     */
    public static class ReportGeneratedReqVO {
        private Long patientId;
        private Long reportId;
        private String reportType;

        // Getters and Setters
        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }
        
        public Long getReportId() { return reportId; }
        public void setReportId(Long reportId) { this.reportId = reportId; }
        
        public String getReportType() { return reportType; }
        public void setReportType(String reportType) { this.reportType = reportType; }
    }

    /**
     * 批量发送请求 VO
     */
    public static class BatchSendReqVO {
        private java.util.List<Long> patientIds;
        private String title;
        private String content;
        private Integer type;

        // Getters and Setters
        public java.util.List<Long> getPatientIds() { 
            return patientIds != null ? new java.util.ArrayList<>(patientIds) : null; 
        }
        public void setPatientIds(java.util.List<Long> patientIds) { 
            this.patientIds = patientIds != null ? new java.util.ArrayList<>(patientIds) : null; 
        }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public Integer getType() { return type; }
        public void setType(Integer type) { this.type = type; }
    }

    /**
     * 免打扰设置请求 VO
     */
    public static class DoNotDisturbReqVO {
        private Long patientId;
        private Boolean enabled;
        private String startTime;
        private String endTime;

        // Getters and Setters
        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }
        
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
    }

}
