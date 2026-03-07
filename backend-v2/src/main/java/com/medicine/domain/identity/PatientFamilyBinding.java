package com.medicine.domain.identity;

import com.medicine.domain.common.BaseEntity;

import java.time.LocalDateTime;

public class PatientFamilyBinding extends BaseEntity {

    private Long patientId;
    private Long familyAccountId;
    private String relationshipType;
    private String status;
    private LocalDateTime authorizedAt;
    private LocalDateTime unboundAt;
    private String unboundReason;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getFamilyAccountId() {
        return familyAccountId;
    }

    public void setFamilyAccountId(Long familyAccountId) {
        this.familyAccountId = familyAccountId;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getAuthorizedAt() {
        return authorizedAt;
    }

    public void setAuthorizedAt(LocalDateTime authorizedAt) {
        this.authorizedAt = authorizedAt;
    }

    public LocalDateTime getUnboundAt() {
        return unboundAt;
    }

    public void setUnboundAt(LocalDateTime unboundAt) {
        this.unboundAt = unboundAt;
    }

    public String getUnboundReason() {
        return unboundReason;
    }

    public void setUnboundReason(String unboundReason) {
        this.unboundReason = unboundReason;
    }
}
