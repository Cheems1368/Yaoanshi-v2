package com.medicine.domain.ai;

import com.medicine.domain.common.BaseEntity;

import java.time.LocalDateTime;

public class AiConsentRecord extends BaseEntity {

    private Long accountId;
    private String policyVersion;
    private String providerCode;
    private String providerDisplayName;
    private String statementHash;
    private LocalDateTime grantedAt;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getPolicyVersion() {
        return policyVersion;
    }

    public void setPolicyVersion(String policyVersion) {
        this.policyVersion = policyVersion;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public String getProviderDisplayName() {
        return providerDisplayName;
    }

    public void setProviderDisplayName(String providerDisplayName) {
        this.providerDisplayName = providerDisplayName;
    }

    public String getStatementHash() {
        return statementHash;
    }

    public void setStatementHash(String statementHash) {
        this.statementHash = statementHash;
    }

    public LocalDateTime getGrantedAt() {
        return grantedAt;
    }

    public void setGrantedAt(LocalDateTime grantedAt) {
        this.grantedAt = grantedAt;
    }
}
