package com.medicine.api.dto.identity;

import java.util.List;

public class AdminPatientListItemDTO {

    private Long id;
    private String name;
    private String mobileMasked;
    private String hospitalPatientId;
    private String bindingStatus;
    private String primaryDiseaseName;
    private Long responsiblePharmacistId;
    private String responsiblePharmacistName;
    private Integer familyBindingCount;
    private List<String> pendingFields;
    private String pendingFieldsSummary;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobileMasked() {
        return mobileMasked;
    }

    public void setMobileMasked(String mobileMasked) {
        this.mobileMasked = mobileMasked;
    }

    public String getHospitalPatientId() {
        return hospitalPatientId;
    }

    public void setHospitalPatientId(String hospitalPatientId) {
        this.hospitalPatientId = hospitalPatientId;
    }

    public String getBindingStatus() {
        return bindingStatus;
    }

    public void setBindingStatus(String bindingStatus) {
        this.bindingStatus = bindingStatus;
    }

    public String getPrimaryDiseaseName() {
        return primaryDiseaseName;
    }

    public void setPrimaryDiseaseName(String primaryDiseaseName) {
        this.primaryDiseaseName = primaryDiseaseName;
    }

    public Long getResponsiblePharmacistId() {
        return responsiblePharmacistId;
    }

    public void setResponsiblePharmacistId(Long responsiblePharmacistId) {
        this.responsiblePharmacistId = responsiblePharmacistId;
    }

    public String getResponsiblePharmacistName() {
        return responsiblePharmacistName;
    }

    public void setResponsiblePharmacistName(String responsiblePharmacistName) {
        this.responsiblePharmacistName = responsiblePharmacistName;
    }

    public Integer getFamilyBindingCount() {
        return familyBindingCount;
    }

    public void setFamilyBindingCount(Integer familyBindingCount) {
        this.familyBindingCount = familyBindingCount;
    }

    public List<String> getPendingFields() {
        return pendingFields;
    }

    public void setPendingFields(List<String> pendingFields) {
        this.pendingFields = pendingFields;
    }

    public String getPendingFieldsSummary() {
        return pendingFieldsSummary;
    }

    public void setPendingFieldsSummary(String pendingFieldsSummary) {
        this.pendingFieldsSummary = pendingFieldsSummary;
    }
}
