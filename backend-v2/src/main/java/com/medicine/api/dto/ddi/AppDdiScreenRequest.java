package com.medicine.api.dto.ddi;

import java.util.List;

public class AppDdiScreenRequest {

    private List<String> newDrugNames;

    public List<String> getNewDrugNames() {
        return newDrugNames;
    }

    public void setNewDrugNames(List<String> newDrugNames) {
        this.newDrugNames = newDrugNames;
    }
}
