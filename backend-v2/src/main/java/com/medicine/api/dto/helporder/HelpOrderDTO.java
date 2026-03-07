package com.medicine.api.dto.helporder;

import java.util.List;

public class HelpOrderDTO {

    private Long id;
    private String orderNo;
    private Long patientId;
    private String patientName;
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
    private String currentHandlerName;
    private Long responsiblePharmacistId;
    private Long assignedDoctorId;
    private String assignedDoctorName;
    private String firstRespondedAt;
    private String closedAt;
    private String rejectReason;
    private String serviceNotice;
    private String createdAt;
    private List<EventDTO> events;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
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

    public String getCurrentHandlerName() {
        return currentHandlerName;
    }

    public void setCurrentHandlerName(String currentHandlerName) {
        this.currentHandlerName = currentHandlerName;
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

    public String getAssignedDoctorName() {
        return assignedDoctorName;
    }

    public void setAssignedDoctorName(String assignedDoctorName) {
        this.assignedDoctorName = assignedDoctorName;
    }

    public String getFirstRespondedAt() {
        return firstRespondedAt;
    }

    public void setFirstRespondedAt(String firstRespondedAt) {
        this.firstRespondedAt = firstRespondedAt;
    }

    public String getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(String closedAt) {
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<EventDTO> getEvents() {
        return events;
    }

    public void setEvents(List<EventDTO> events) {
        this.events = events;
    }

    public static class EventDTO {
        private Long id;
        private String eventType;
        private String fromStatus;
        private String toStatus;
        private String operatorType;
        private Long operatorId;
        private String operatorName;
        private String content;
        private Long attachmentFileAssetId;
        private String eventAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public String getFromStatus() {
            return fromStatus;
        }

        public void setFromStatus(String fromStatus) {
            this.fromStatus = fromStatus;
        }

        public String getToStatus() {
            return toStatus;
        }

        public void setToStatus(String toStatus) {
            this.toStatus = toStatus;
        }

        public String getOperatorType() {
            return operatorType;
        }

        public void setOperatorType(String operatorType) {
            this.operatorType = operatorType;
        }

        public Long getOperatorId() {
            return operatorId;
        }

        public void setOperatorId(Long operatorId) {
            this.operatorId = operatorId;
        }

        public String getOperatorName() {
            return operatorName;
        }

        public void setOperatorName(String operatorName) {
            this.operatorName = operatorName;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Long getAttachmentFileAssetId() {
            return attachmentFileAssetId;
        }

        public void setAttachmentFileAssetId(Long attachmentFileAssetId) {
            this.attachmentFileAssetId = attachmentFileAssetId;
        }

        public String getEventAt() {
            return eventAt;
        }

        public void setEventAt(String eventAt) {
            this.eventAt = eventAt;
        }
    }
}
