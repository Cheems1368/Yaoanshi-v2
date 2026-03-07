package com.medicine.domain.identity;

import com.medicine.domain.common.BaseEntity;

public class DoctorProfile extends BaseEntity {

    private Long systemUserId;
    private String specialtyCode;
    private String titleName;
    private String realName;
    private String mobile;
    private String serviceStatus;
    private String workHoursJson;

    public Long getSystemUserId() {
        return systemUserId;
    }

    public void setSystemUserId(Long systemUserId) {
        this.systemUserId = systemUserId;
    }

    public String getSpecialtyCode() {
        return specialtyCode;
    }

    public void setSpecialtyCode(String specialtyCode) {
        this.specialtyCode = specialtyCode;
    }

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(String serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public String getWorkHoursJson() {
        return workHoursJson;
    }

    public void setWorkHoursJson(String workHoursJson) {
        this.workHoursJson = workHoursJson;
    }
}
