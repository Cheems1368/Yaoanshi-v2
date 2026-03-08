package com.medicine.service.admin;

import com.medicine.api.dto.ddi.AdminDdiRuleDTO;
import com.medicine.api.dto.ddi.AdminDdiRuleUpsertRequest;
import com.medicine.api.dto.ddi.DrugContraindicationDTO;
import com.medicine.api.dto.ddi.DrugContraindicationUpsertRequest;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.model.PageResult;
import com.medicine.domain.ddi.DdiRule;
import com.medicine.domain.ddi.DrugContraindication;
import com.medicine.domain.identity.PharmacistProfile;
import com.medicine.repository.ddi.DdiRuleMapper;
import com.medicine.repository.ddi.DrugContraindicationMapper;
import com.medicine.service.auth.JwtClaims;
import com.medicine.service.auth.ProfessionalIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDdiServiceTest {

    @Mock
    private DdiRuleMapper ddiRuleMapper;

    @Mock
    private DrugContraindicationMapper drugContraindicationMapper;

    @Mock
    private ProfessionalIdentityService professionalIdentityService;

    @InjectMocks
    private AdminDdiService adminDdiService;

    @Test
    void shouldReturnPagedRuleList() {
        JwtClaims claims = claims();
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(profile());
        when(ddiRuleMapper.countSearch(1L, "aspirin", "HIGH")).thenReturn(1L);
        when(ddiRuleMapper.search(1L, "aspirin", "HIGH", 0, 10)).thenReturn(List.of(rule()));

        PageResult<AdminDdiRuleDTO> result = adminDdiService.listRules(claims, " aspirin ", "HIGH", 1, 10);

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getList().get(0).getRuleCode()).isEqualTo("DDI-001");
        verify(professionalIdentityService).getOrCreatePharmacistProfile(claims);
    }

    @Test
    void shouldCreateRule() {
        JwtClaims claims = claims();
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(profile());
        when(ddiRuleMapper.insert(any(DdiRule.class))).thenAnswer(invocation -> {
            DdiRule value = invocation.getArgument(0);
            value.setId(301L);
            return 1;
        });

        AdminDdiRuleDTO result = adminDdiService.createRule(claims, ruleRequest());

        assertThat(result.getId()).isEqualTo(301L);
        assertThat(result.getRuleCode()).isEqualTo("DDI-NEW");
        assertThat(result.getSeverity()).isEqualTo("HIGH");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(professionalIdentityService).getOrCreatePharmacistProfile(claims);
    }

    @Test
    void shouldUpdateRule() {
        JwtClaims claims = claims();
        DdiRule existing = rule();
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(profile());
        when(ddiRuleMapper.selectById(1L, 101L)).thenReturn(existing);

        AdminDdiRuleUpsertRequest request = new AdminDdiRuleUpsertRequest();
        request.setDrugAName("Aspirin");
        request.setDrugBName("Warfarin");
        request.setSeverity("medium");
        request.setMessage("Updated message");
        request.setRecommendedAction("Observe closely");
        request.setStatus("inactive");

        AdminDdiRuleDTO result = adminDdiService.updateRule(claims, 101L, request);

        assertThat(result.getId()).isEqualTo(101L);
        assertThat(result.getSeverity()).isEqualTo("MEDIUM");
        assertThat(result.getStatus()).isEqualTo("INACTIVE");
        assertThat(result.getMessage()).isEqualTo("Updated message");
        verify(professionalIdentityService).getOrCreatePharmacistProfile(claims);
    }

    @Test
    void shouldRejectUpdatingMissingRule() {
        JwtClaims claims = claims();
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(profile());
        when(ddiRuleMapper.selectById(1L, 999L)).thenReturn(null);

        assertThatThrownBy(() -> adminDdiService.updateRule(claims, 999L, ruleRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("DDI_RULE_NOT_FOUND");
    }

    @Test
    void shouldReturnPagedContraindicationList() {
        JwtClaims claims = claims();
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(profile());
        when(drugContraindicationMapper.countSearch(1L, "metformin", "HIGH", "DISEASE")).thenReturn(1L);
        when(drugContraindicationMapper.search(1L, "metformin", "HIGH", "DISEASE", 0, 10))
                .thenReturn(List.of(contraindication()));

        PageResult<DrugContraindicationDTO> result = adminDdiService.listContraindications(
                claims,
                " metformin ",
                "HIGH",
                "DISEASE",
                1,
                10
        );

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getList().get(0).getRuleCode()).isEqualTo("CONTRA-001");
        verify(professionalIdentityService).getOrCreatePharmacistProfile(claims);
    }

    @Test
    void shouldCreateContraindication() {
        JwtClaims claims = claims();
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(profile());
        when(drugContraindicationMapper.insert(any(DrugContraindication.class))).thenAnswer(invocation -> {
            DrugContraindication value = invocation.getArgument(0);
            value.setId(401L);
            return 1;
        });

        DrugContraindicationDTO result = adminDdiService.createContraindication(claims, contraindicationRequest());

        assertThat(result.getId()).isEqualTo(401L);
        assertThat(result.getRuleCode()).isEqualTo("CONTRA-NEW");
        assertThat(result.getConditionType()).isEqualTo("DISEASE");
        assertThat(result.getSeverity()).isEqualTo("HIGH");
        verify(professionalIdentityService).getOrCreatePharmacistProfile(claims);
    }

    @Test
    void shouldUpdateContraindication() {
        JwtClaims claims = claims();
        DrugContraindication existing = contraindication();
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(profile());
        when(drugContraindicationMapper.selectById(1L, 201L)).thenReturn(existing);

        DrugContraindicationUpsertRequest request = new DrugContraindicationUpsertRequest();
        request.setDrugName("Metformin");
        request.setConditionType("organ_function");
        request.setConditionValue("severe_renal_impairment");
        request.setSeverity("medium");
        request.setMessage("Updated warning");
        request.setRecommendedAction("Adjust dose");
        request.setStatus("inactive");

        DrugContraindicationDTO result = adminDdiService.updateContraindication(claims, 201L, request);

        assertThat(result.getId()).isEqualTo(201L);
        assertThat(result.getConditionType()).isEqualTo("ORGAN_FUNCTION");
        assertThat(result.getSeverity()).isEqualTo("MEDIUM");
        assertThat(result.getStatus()).isEqualTo("INACTIVE");
        assertThat(result.getMessage()).isEqualTo("Updated warning");
        verify(professionalIdentityService).getOrCreatePharmacistProfile(claims);
    }

    @Test
    void shouldRejectUpdatingMissingContraindication() {
        JwtClaims claims = claims();
        when(professionalIdentityService.getOrCreatePharmacistProfile(claims)).thenReturn(profile());
        when(drugContraindicationMapper.selectById(1L, 999L)).thenReturn(null);

        assertThatThrownBy(() -> adminDdiService.updateContraindication(claims, 999L, contraindicationRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("DDI_CONTRAINDICATION_NOT_FOUND");
    }

    private JwtClaims claims() {
        JwtClaims claims = new JwtClaims();
        claims.setTenantId(1L);
        claims.setUserId(100L);
        claims.setAccountType("PROFESSIONAL");
        claims.setRoles(List.of("pharmacists"));
        return claims;
    }

    private PharmacistProfile profile() {
        PharmacistProfile profile = new PharmacistProfile();
        profile.setId(9001L);
        profile.setTenantId(1L);
        return profile;
    }

    private DdiRule rule() {
        DdiRule rule = new DdiRule();
        rule.setId(101L);
        rule.setRuleCode("DDI-001");
        rule.setDrugAName("Aspirin");
        rule.setDrugBName("Clopidogrel");
        rule.setSeverity("HIGH");
        rule.setMessage("Bleeding risk");
        rule.setRecommendedAction("Pharmacist review required");
        rule.setStatus("ACTIVE");
        rule.setMaintainedByPharmacistId(9001L);
        rule.setTenantId(1L);
        rule.setUpdateTime(LocalDateTime.of(2026, 3, 8, 10, 0));
        return rule;
    }

    private DrugContraindication contraindication() {
        DrugContraindication rule = new DrugContraindication();
        rule.setId(201L);
        rule.setRuleCode("CONTRA-001");
        rule.setDrugName("Metformin");
        rule.setConditionType("DISEASE");
        rule.setConditionValue("renal_impairment");
        rule.setSeverity("HIGH");
        rule.setMessage("Avoid in severe renal impairment");
        rule.setRecommendedAction("Use alternative therapy");
        rule.setStatus("ACTIVE");
        rule.setMaintainedByPharmacistId(9001L);
        rule.setTenantId(1L);
        rule.setUpdateTime(LocalDateTime.of(2026, 3, 8, 10, 0));
        return rule;
    }

    private AdminDdiRuleUpsertRequest ruleRequest() {
        AdminDdiRuleUpsertRequest request = new AdminDdiRuleUpsertRequest();
        request.setRuleCode("DDI-NEW");
        request.setDrugAName("Aspirin");
        request.setDrugBName("Warfarin");
        request.setSeverity("high");
        request.setMessage("Concurrent use increases bleeding risk");
        request.setRecommendedAction("Confirm necessity before continuing");
        request.setStatus("active");
        return request;
    }

    private DrugContraindicationUpsertRequest contraindicationRequest() {
        DrugContraindicationUpsertRequest request = new DrugContraindicationUpsertRequest();
        request.setRuleCode("CONTRA-NEW");
        request.setDrugName("Metformin");
        request.setConditionType("disease");
        request.setConditionValue("renal_impairment");
        request.setSeverity("high");
        request.setMessage("Avoid in severe renal impairment");
        request.setRecommendedAction("Use alternative therapy");
        request.setStatus("active");
        return request;
    }
}
