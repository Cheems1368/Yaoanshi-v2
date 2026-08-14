package com.medicine.api.dto.ddi;

import java.util.List;

public class AdminDdiScreenResult {

    private List<AdminDdiRiskCardDTO> alerts;

    public List<AdminDdiRiskCardDTO> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<AdminDdiRiskCardDTO> alerts) {
        this.alerts = alerts;
    }
}
