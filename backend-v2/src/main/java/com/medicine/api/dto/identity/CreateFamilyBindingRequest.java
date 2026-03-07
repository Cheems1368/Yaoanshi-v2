package com.medicine.api.dto.identity;

public class CreateFamilyBindingRequest {

    private String familyMobile;
    private String familyDisplayName;
    private String relationshipType;

    public String getFamilyMobile() {
        return familyMobile;
    }

    public void setFamilyMobile(String familyMobile) {
        this.familyMobile = familyMobile;
    }

    public String getFamilyDisplayName() {
        return familyDisplayName;
    }

    public void setFamilyDisplayName(String familyDisplayName) {
        this.familyDisplayName = familyDisplayName;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }
}
