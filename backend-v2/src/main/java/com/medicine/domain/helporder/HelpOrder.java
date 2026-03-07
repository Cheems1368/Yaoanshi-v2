package com.medicine.domain.helporder;

import com.medicine.domain.common.BaseEntity;

import java.time.LocalDateTime;

public class HelpOrder extends BaseEntity {

    private String orderNo;
    private Long patientId;
    private String submittedByType;
    private Long submittedById;
    private String category;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Boolean requiresOfflineEmergency;
    private String currentHandlerType;
    private Long currentHandlerId;
    private Long responsiblePharmacistId;
    private Long assignedDoctorId;
    private LocalDateTime firstRespondedAt;
    private LocalDateTime closedAt;
    private String rejectReason;
    private String serviceNotice;
    private String patientName;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getSubmittedByType() {
        return submittedByType;
    }

    public void setSubmittedByType(String submittedByType) {
        this.submittedByType = submittedByType;
    }

    public Long getSubmittedById() {
        return submittedById;
    }

    public void setSubmittedById(Long submittedById) {
        this.submittedById = submittedById;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Boolean getRequiresOfflineEmergency() {
        return requiresOfflineEmergency;
    }

    public void setRequiresOfflineEmergency(Boolean requiresOfflineEmergency) {
        this.requiresOfflineEmergency = requiresOfflineEmergency;
    }

    public String getCurrentHandlerType() {
        return currentHandlerType;
    }

    public void setCurrentHandlerType(String currentHandlerType) {
        this.currentHandlerType = currentHandlerType;
    }

    public Long getCurrentHandlerId() {
        return currentHandlerId;
    }

    public void setCurrentHandlerId(Long currentHandlerId) {
        this.currentHandlerId = currentHandlerId;
    }

    public Long getResponsiblePharmacistId() {
        return responsiblePharmacistId;
    }

    public void setResponsiblePharmacistId(Long responsiblePharmacistId) {
        this.responsiblePharmacistId = responsiblePharmacistId;
    }

    public Long getAssignedDoctorId() {
        return assignedDoctorId;
    }

    public void setAssignedDoctorId(Long assignedDoctorId) {
        this.assignedDoctorId = assignedDoctorId;
    }

    public LocalDateTime getFirstRespondedAt() {
        return firstRespondedAt;
    }

    public void setFirstRespondedAt(LocalDateTime firstRespondedAt) {
        this.firstRespondedAt = firstRespondedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getServiceNotice() {
        return serviceNotice;
    }

    public void setServiceNotice(String serviceNotice) {
        this.serviceNotice = serviceNotice;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
}
