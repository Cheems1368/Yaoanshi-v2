package com.medicine.api.dto.identity;

public class PatientAdherenceSummaryDTO {

    private String status;
    private Double sevenDayAdherence;
    private Double thirtyDayAdherence;
    private String asOf;
    private Long sevenDayDueTasks;
    private Long sevenDayOnTimeTaken;
    private Long sevenDayEarlyOver60Minutes;
    private Long sevenDayPendingOnTimeWindow;
    private Long thirtyDayDueTasks;
    private Long thirtyDayOnTimeTaken;
    private Long thirtyDayEarlyOver60Minutes;
    private Long thirtyDayPendingOnTimeWindow;

    public String getAsOf() { return asOf; }
    public void setAsOf(String asOf) { this.asOf = asOf; }
    public Long getSevenDayDueTasks() { return sevenDayDueTasks; }
    public void setSevenDayDueTasks(Long value) { this.sevenDayDueTasks = value; }
    public Long getSevenDayOnTimeTaken() { return sevenDayOnTimeTaken; }
    public void setSevenDayOnTimeTaken(Long value) { this.sevenDayOnTimeTaken = value; }
    public Long getSevenDayEarlyOver60Minutes() { return sevenDayEarlyOver60Minutes; }
    public void setSevenDayEarlyOver60Minutes(Long value) { this.sevenDayEarlyOver60Minutes = value; }
    public Long getSevenDayPendingOnTimeWindow() { return sevenDayPendingOnTimeWindow; }
    public void setSevenDayPendingOnTimeWindow(Long value) { this.sevenDayPendingOnTimeWindow = value; }
    public Long getThirtyDayDueTasks() { return thirtyDayDueTasks; }
    public void setThirtyDayDueTasks(Long value) { this.thirtyDayDueTasks = value; }
    public Long getThirtyDayOnTimeTaken() { return thirtyDayOnTimeTaken; }
    public void setThirtyDayOnTimeTaken(Long value) { this.thirtyDayOnTimeTaken = value; }
    public Long getThirtyDayEarlyOver60Minutes() { return thirtyDayEarlyOver60Minutes; }
    public void setThirtyDayEarlyOver60Minutes(Long value) { this.thirtyDayEarlyOver60Minutes = value; }
    public Long getThirtyDayPendingOnTimeWindow() { return thirtyDayPendingOnTimeWindow; }
    public void setThirtyDayPendingOnTimeWindow(Long value) { this.thirtyDayPendingOnTimeWindow = value; }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getSevenDayAdherence() {
        return sevenDayAdherence;
    }

    public void setSevenDayAdherence(Double sevenDayAdherence) {
        this.sevenDayAdherence = sevenDayAdherence;
    }

    public Double getThirtyDayAdherence() {
        return thirtyDayAdherence;
    }

    public void setThirtyDayAdherence(Double thirtyDayAdherence) {
        this.thirtyDayAdherence = thirtyDayAdherence;
    }
}
