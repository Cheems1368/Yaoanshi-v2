package com.medicine.api.dto.ddi;

import java.util.List;

public class AppDdiScreenResult {

    private List<AppDdiAlert> alerts;

    public List<AppDdiAlert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<AppDdiAlert> alerts) {
        this.alerts = alerts;
    }
}
