package com.medicine.domain.medication;

import com.medicine.domain.common.BaseEntity;

public class SelfReportedMedication extends BaseEntity {

    private Long patientId;
    private String drugName;
    private String dosage;
    /** PENDING / ACTIVE / STOPPED / TRANSFERRED */
    private String status;
    private String notes;
    /** PATIENT / FAMILY */
    private String operatorType;
    private Long operatorAccountId;
    private Long workOrderId;
    private Long transferredPlanItemId;

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getDrugName() { return drugName; }
    public void setDrugName(String drugName) { this.drugName = drugName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getOperatorType() { return operatorType; }
    public void setOperatorType(String operatorType) { this.operatorType = operatorType; }

    public Long getOperatorAccountId() { return operatorAccountId; }
    public void setOperatorAccountId(Long operatorAccountId) { this.operatorAccountId = operatorAccountId; }

    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }

    public Long getTransferredPlanItemId() { return transferredPlanItemId; }
    public void setTransferredPlanItemId(Long transferredPlanItemId) { this.transferredPlanItemId = transferredPlanItemId; }
}
