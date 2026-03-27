package com.medicine.api.dto.medication;

import java.util.List;

public class SelfReportedMedicationCreateRequest {

    private List<DrugEntry> drugs;
    private String notes;

    public List<DrugEntry> getDrugs() { return drugs; }
    public void setDrugs(List<DrugEntry> drugs) { this.drugs = drugs; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public static class DrugEntry {
        private String drugName;
        private String dosage;

        public String getDrugName() { return drugName; }
        public void setDrugName(String drugName) { this.drugName = drugName; }

        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }
    }
}
