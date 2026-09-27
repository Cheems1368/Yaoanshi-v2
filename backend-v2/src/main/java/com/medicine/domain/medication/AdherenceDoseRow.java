package com.medicine.domain.medication;

import java.time.LocalDateTime;

public class AdherenceDoseRow {
    private LocalDateTime scheduledAt;
    private String status;
    private String result;
    private LocalDateTime actualTakenAt;

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public LocalDateTime getActualTakenAt() { return actualTakenAt; }
    public void setActualTakenAt(LocalDateTime actualTakenAt) { this.actualTakenAt = actualTakenAt; }
}
