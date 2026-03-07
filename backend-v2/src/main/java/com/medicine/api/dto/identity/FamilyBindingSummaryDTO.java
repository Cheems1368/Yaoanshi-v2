package com.medicine.api.dto.identity;

public class FamilyBindingSummaryDTO {

    private Long id;
    private Long familyAccountId;
    private String relationshipType;
    private String status;
    private String authorizedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFamilyAccountId() {
        return familyAccountId;
    }

    public void setFamilyAccountId(Long familyAccountId) {
        this.familyAccountId = familyAccountId;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuthorizedAt() {
        return authorizedAt;
    }

    public void setAuthorizedAt(String authorizedAt) {
        this.authorizedAt = authorizedAt;
    }
}
