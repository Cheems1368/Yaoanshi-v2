package com.medicine.api.dto.medication;

import java.util.List;

public class MedicationPlanDTO {

    private Long id;
    private Long patientId;
    private String patientName;
    private String planName;
    private Integer versionNo;
    private String status;
    private String sourceType;
    private Long sourcePrescriptionId;
    private String effectiveFrom;
    private String effectiveTo;
    private String notes;
    private Long createdByPharmacistId;
    private String confirmedAt;
    private String createdAt;
    private List<ItemDTO> items;
    private List<EventDTO> events;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourcePrescriptionId() {
        return sourcePrescriptionId;
    }

    public void setSourcePrescriptionId(Long sourcePrescriptionId) {
        this.sourcePrescriptionId = sourcePrescriptionId;
    }

    public String getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(String effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public String getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(String effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getCreatedByPharmacistId() {
        return createdByPharmacistId;
    }

    public void setCreatedByPharmacistId(Long createdByPharmacistId) {
        this.createdByPharmacistId = createdByPharmacistId;
    }

    public String getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(String confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<ItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemDTO> items) {
        this.items = items;
    }

    public List<EventDTO> getEvents() {
        return events;
    }

    public void setEvents(List<EventDTO> events) {
        this.events = events;
    }

    public static class ItemDTO {
        private Long id;
        private String drugName;
        private String drugSpec;
        private String dosage;
        private String frequencyCode;
        private List<String> timePoints;
        private String mealRelation;
        private String route;
        private String instructions;
        private Integer sortOrder;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getDrugName() {
            return drugName;
        }

        public void setDrugName(String drugName) {
            this.drugName = drugName;
        }

        public String getDrugSpec() {
            return drugSpec;
        }

        public void setDrugSpec(String drugSpec) {
            this.drugSpec = drugSpec;
        }

        public String getDosage() {
            return dosage;
        }

        public void setDosage(String dosage) {
            this.dosage = dosage;
        }

        public String getFrequencyCode() {
            return frequencyCode;
        }

        public void setFrequencyCode(String frequencyCode) {
            this.frequencyCode = frequencyCode;
        }

        public List<String> getTimePoints() {
            return timePoints;
        }

        public void setTimePoints(List<String> timePoints) {
            this.timePoints = timePoints;
        }

        public String getMealRelation() {
            return mealRelation;
        }

        public void setMealRelation(String mealRelation) {
            this.mealRelation = mealRelation;
        }

        public String getRoute() {
            return route;
        }

        public void setRoute(String route) {
            this.route = route;
        }

        public String getInstructions() {
            return instructions;
        }

        public void setInstructions(String instructions) {
            this.instructions = instructions;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }
    }

    public static class EventDTO {
        private Long id;
        private String eventType;
        private String content;
        private String eventAt;
        private String operatorType;
        private Long operatorId;

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

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getEventAt() {
            return eventAt;
        }

        public void setEventAt(String eventAt) {
            this.eventAt = eventAt;
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
    }
}
