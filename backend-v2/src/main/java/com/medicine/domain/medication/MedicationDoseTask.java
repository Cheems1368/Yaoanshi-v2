package com.medicine.domain.medication;

import com.medicine.domain.common.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MedicationDoseTask extends BaseEntity {

    private Long patientId;
    private Long planId;
    private Long planItemId;
    private LocalDate taskDate;
    private LocalDateTime scheduledAt;
    private String status;
    private String notificationStatus;
    private Long lastEffectiveCheckinId;
    private LocalDateTime lockedAfter;
    private String drugName;
    private String dosage;
    private String frequencyCode;
    private String route;
    private String mealRelation;
    private String instructions;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public Long getPlanItemId() {
        return planItemId;
    }

    public void setPlanItemId(Long planItemId) {
        this.planItemId = planItemId;
    }

    public LocalDate getTaskDate() {
        return taskDate;
    }

    public void setTaskDate(LocalDate taskDate) {
        this.taskDate = taskDate;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(String notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public Long getLastEffectiveCheckinId() {
        return lastEffectiveCheckinId;
    }

    public void setLastEffectiveCheckinId(Long lastEffectiveCheckinId) {
        this.lastEffectiveCheckinId = lastEffectiveCheckinId;
    }

    public LocalDateTime getLockedAfter() {
        return lockedAfter;
    }

    public void setLockedAfter(LocalDateTime lockedAfter) {
        this.lockedAfter = lockedAfter;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
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

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getMealRelation() {
        return mealRelation;
    }

    public void setMealRelation(String mealRelation) {
        this.mealRelation = mealRelation;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
