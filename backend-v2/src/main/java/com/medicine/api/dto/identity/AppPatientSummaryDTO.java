package com.medicine.api.dto.identity;

public class AppPatientSummaryDTO {

    private Long id;
    private String name;
    private String mobile;
    private String hospitalPatientId;
    private String bindingStatus;
    private boolean bound;
    private String boundAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getHospitalPatientId() { return hospitalPatientId; }
    public void setHospitalPatientId(String hospitalPatientId) { this.hospitalPatientId = hospitalPatientId; }

    public String getBindingStatus() { return bindingStatus; }
    public void setBindingStatus(String bindingStatus) { this.bindingStatus = bindingStatus; }

    public boolean isBound() { return bound; }
    public void setBound(boolean bound) { this.bound = bound; }

    public String getBoundAt() { return boundAt; }
    public void setBoundAt(String boundAt) { this.boundAt = boundAt; }
}
