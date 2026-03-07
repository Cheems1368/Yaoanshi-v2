package com.medicine.api.dto.auth;

public class OtpSendRequest {

    private String mobile;
    private String scene;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }
}
