package com.medicine.api.dto.medication;

public class TransferPrefillDTO {

    private String drugName;
    private String dosage;
    private Long patientId;
    private Long selfReportedMedicationId;

    public String getDrugName() { return drugName; }
    public void setDrugName(String drugName) { this.drugName = drugName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getSelfReportedMedicationId() { return selfReportedMedicationId; }
    public void setSelfReportedMedicationId(Long selfReportedMedicationId) { this.selfReportedMedicationId = selfReportedMedicationId; }
}
