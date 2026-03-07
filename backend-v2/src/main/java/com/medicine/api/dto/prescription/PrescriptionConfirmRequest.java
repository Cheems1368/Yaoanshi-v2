package com.medicine.api.dto.prescription;

import java.util.List;

public class PrescriptionConfirmRequest {

    private List<ItemInput> items;

    public List<ItemInput> getItems() {
        return items;
    }

    public void setItems(List<ItemInput> items) {
        this.items = items;
    }

    public static class ItemInput {
        private String drugName;
        private String drugSpec;
        private String dosage;
        private String frequency;
        private String route;
        private Integer durationDays;

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

        public String getFrequency() {
            return frequency;
        }

        public void setFrequency(String frequency) {
            this.frequency = frequency;
        }

        public String getRoute() {
            return route;
        }

        public void setRoute(String route) {
            this.route = route;
        }

        public Integer getDurationDays() {
            return durationDays;
        }

        public void setDurationDays(Integer durationDays) {
            this.durationDays = durationDays;
        }
    }
}
