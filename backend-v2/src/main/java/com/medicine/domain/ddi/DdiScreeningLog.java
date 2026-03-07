package com.medicine.domain.ddi;

import com.medicine.domain.common.BaseEntity;

import java.time.LocalDateTime;

public class DdiScreeningLog extends BaseEntity {

    private Long patientId;
    private Long prescriptionId;
    private Long medicationPlanId;
    private Long matchedRuleId;
    private String severity;
    private String confirmStatus;
    private Long confirmedByPharmacistId;
    private LocalDateTime confirmedAt;
    private String confirmationNote;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public Long getMedicationPlanId() {
        return medicationPlanId;
    }

    public void setMedicationPlanId(Long medicationPlanId) {
        this.medicationPlanId = medicationPlanId;
    }

    public Long getMatchedRuleId() {
        return matchedRuleId;
    }

    public void setMatchedRuleId(Long matchedRuleId) {
        this.matchedRuleId = matchedRuleId;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getConfirmStatus() {
        return confirmStatus;
    }

    public void setConfirmStatus(String confirmStatus) {
        this.confirmStatus = confirmStatus;
    }

    public Long getConfirmedByPharmacistId() {
        return confirmedByPharmacistId;
    }

    public void setConfirmedByPharmacistId(Long confirmedByPharmacistId) {
        this.confirmedByPharmacistId = confirmedByPharmacistId;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getConfirmationNote() {
        return confirmationNote;
    }

    public void setConfirmationNote(String confirmationNote) {
        this.confirmationNote = confirmationNote;
    }
}
