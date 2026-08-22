package com.medicine.service.ai;

import com.medicine.api.dto.ai.AiConsentGrantRequest;
import com.medicine.api.dto.ai.AiDisclosureDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.domain.ai.AiConsentRecord;
import com.medicine.domain.identity.AppAccount;
import com.medicine.repository.ai.AiConsentRecordMapper;
import com.medicine.repository.identity.AppAccountMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class AiConsentService {

    private static final long DEFAULT_TENANT_ID = 1L;

    private final AppAccountMapper appAccountMapper;
    private final AiConsentRecordMapper consentRecordMapper;
    private final AiConsentPolicy policy;

    public AiConsentService(AppAccountMapper appAccountMapper,
                            AiConsentRecordMapper consentRecordMapper,
                            AiConsentPolicy policy) {
        this.appAccountMapper = appAccountMapper;
        this.consentRecordMapper = consentRecordMapper;
        this.policy = policy;
    }

    public AiDisclosureDTO getDisclosure(String mobile) {
        AiDisclosureDTO disclosure = new AiDisclosureDTO();
        disclosure.setPolicyVersion(policy.version());
        disclosure.setProviderCode(policy.providerCode());
        disclosure.setProviderDisplayName(policy.providerDisplayName());
        disclosure.setStatement(policy.statement());
        disclosure.setConsentGranted(hasCurrentConsent(mobile));
        return disclosure;
    }

    public boolean hasCurrentConsent(String mobile) {
        AppAccount account = resolveAccount(mobile);
        if (account == null) {
            return false;
        }
        return consentRecordMapper.selectCurrent(
                tenantId(account), account.getId(), policy.version(), policy.providerCode(), policy.statementHash()
        ) != null;
    }

    public AppAccount requireCurrentConsent(String mobile) {
        AppAccount account = resolveAccount(mobile);
        if (account == null || consentRecordMapper.selectCurrent(
                tenantId(account), account.getId(), policy.version(), policy.providerCode(), policy.statementHash()
        ) == null) {
            throw new BusinessException(
                    ErrorCode.AI_CONSENT_REQUIRED.getCode(),
                    "请先阅读并同意当前版本的 AI 助手数据共享声明"
            );
        }
        return account;
    }

    @Transactional
    public void grantConsent(String mobile, AiConsentGrantRequest request) {
        if (request == null || !StringUtils.hasText(request.getPolicyVersion())
                || !policy.version().equals(request.getPolicyVersion().trim())) {
            throw new BusinessException(
                    ErrorCode.AI_CONSENT_POLICY_MISMATCH.getCode(),
                    "数据共享声明已更新，请刷新后重新确认"
            );
        }

        AppAccount account = resolveAccount(mobile);
        if (account == null) {
            throw new BusinessException(ErrorCode.AI_CONSENT_REQUIRED.getCode(), "当前账户不可用");
        }

        LocalDateTime now = LocalDateTime.now();
        AiConsentRecord record = new AiConsentRecord();
        record.setAccountId(account.getId());
        record.setPolicyVersion(policy.version());
        record.setProviderCode(policy.providerCode());
        record.setProviderDisplayName(policy.providerDisplayName());
        record.setStatementHash(policy.statementHash());
        record.setGrantedAt(now);
        record.setCreator(String.valueOf(account.getId()));
        record.setCreateTime(now);
        record.setUpdater(String.valueOf(account.getId()));
        record.setUpdateTime(now);
        record.setDeleted(false);
        record.setTenantId(tenantId(account));
        consentRecordMapper.insert(record);

        // Keep the legacy timestamp populated for compatibility; it is not sufficient for v2 consent checks.
        appAccountMapper.updateAiConsent(account.getId(), now);
    }

    private AppAccount resolveAccount(String mobile) {
        return appAccountMapper.selectActiveByMobile(DEFAULT_TENANT_ID, mobile);
    }

    private long tenantId(AppAccount account) {
        return account.getTenantId() == null ? DEFAULT_TENANT_ID : account.getTenantId();
    }
}
