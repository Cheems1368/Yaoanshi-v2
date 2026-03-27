package com.medicine.service.app;

import com.medicine.api.dto.ddi.AppDdiScreenRequest;
import com.medicine.api.dto.ddi.AppDdiScreenResult;
import com.medicine.common.exception.BusinessException;
import com.medicine.domain.ddi.DdiRule;
import com.medicine.domain.ddi.DrugContraindication;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.identity.PatientHealthProfile;
import com.medicine.domain.medication.MedicationPlan;
import com.medicine.domain.medication.MedicationPlanItem;
import com.medicine.repository.ddi.DdiRuleMapper;
import com.medicine.repository.ddi.DrugContraindicationMapper;
import com.medicine.repository.identity.PatientHealthProfileMapper;
import com.medicine.repository.medication.MedicationPlanItemMapper;
import com.medicine.repository.medication.MedicationPlanMapper;
import com.medicine.repository.medication.SelfReportedMedicationMapper;
import com.medicine.service.auth.AppPatientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppDdiScreeningServiceTest {

    @Mock
    private AppPatientService appPatientService;

    @Mock
    private MedicationPlanMapper medicationPlanMapper;

    @Mock
    private MedicationPlanItemMapper medicationPlanItemMapper;

    @Mock
    private DdiRuleMapper ddiRuleMapper;

    @Mock
    private DrugContraindicationMapper drugContraindicationMapper;

    @Mock
    private PatientHealthProfileMapper patientHealthProfileMapper;

    @Mock
    private SelfReportedMedicationMapper selfReportedMedicationMapper;

    @InjectMocks
    private AppDdiScreeningService appDdiScreeningService;

    @Test
    void shouldReturnContraindicationsBeforeMediumInteractions() {
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());

        MedicationPlan plan = new MedicationPlan();
        plan.setId(601L);
        when(medicationPlanMapper.selectActiveByPatientId(1L, 201L)).thenReturn(plan);

        MedicationPlanItem item = new MedicationPlanItem();
        item.setDrugName("氯吡格雷片");
        when(medicationPlanItemMapper.selectByPlanId(1L, 601L)).thenReturn(List.of(item));

        DdiRule interactionRule = new DdiRule();
        interactionRule.setDrugAName("氯吡格雷");
        interactionRule.setDrugBName("奥美拉唑");
        interactionRule.setSeverity("MEDIUM");
        interactionRule.setMessage("奥美拉唑可能降低氯吡格雷抗血小板效果。");
        interactionRule.setRecommendedAction("建议更换胃药。");
        when(ddiRuleMapper.selectActiveRules(1L)).thenReturn(List.of(interactionRule));

        DrugContraindication contraindication = new DrugContraindication();
        contraindication.setDrugName("二甲双胍");
        contraindication.setConditionType("ORGAN_FUNCTION");
        contraindication.setConditionValue("严重肾功能不全");
        contraindication.setSeverity("HIGH");
        contraindication.setMessage("严重肾功能不全患者使用二甲双胍风险高。");
        contraindication.setRecommendedAction("请先咨询医生。");
        when(drugContraindicationMapper.selectActiveRules(1L)).thenReturn(List.of(contraindication));

        PatientHealthProfile healthProfile = new PatientHealthProfile();
        healthProfile.setPatientId(201L);
        healthProfile.setLayer2LiverKidney("既往评估提示严重肾功能不全");
        when(patientHealthProfileMapper.selectByPatientId(1L, 201L)).thenReturn(healthProfile);

        AppDdiScreenRequest request = new AppDdiScreenRequest();
        request.setNewDrugNames(List.of("奥美拉唑", "二甲双胍"));

        AppDdiScreenResult result = appDdiScreeningService.screenForSubject("13800138000", request);

        assertThat(result.getAlerts()).hasSize(2);
        assertThat(result.getAlerts().get(0).getType()).isEqualTo("CONTRAINDICATION");
        assertThat(result.getAlerts().get(0).getSeverity()).isEqualTo("HIGH");
        assertThat(result.getAlerts().get(0).getDrugA()).isEqualTo("严重肾功能不全");
        assertThat(result.getAlerts().get(0).getDrugB()).isEqualTo("二甲双胍");
        assertThat(result.getAlerts().get(1).getType()).isEqualTo("INTERACTION");
        assertThat(result.getAlerts().get(1).getSeverity()).isEqualTo("MEDIUM");
    }

    @Test
    void shouldMatchDrugClassKeywordsForContraindications() {
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());
        when(medicationPlanMapper.selectActiveByPatientId(1L, 201L)).thenReturn(null);
        when(ddiRuleMapper.selectActiveRules(1L)).thenReturn(List.of());

        DrugContraindication contraindication = new DrugContraindication();
        contraindication.setDrugName("ACEI类");
        contraindication.setConditionType("DISEASE");
        contraindication.setConditionValue("血管性水肿史");
        contraindication.setSeverity("HIGH");
        contraindication.setMessage("既往血管性水肿患者使用 ACEI 风险高。");
        contraindication.setRecommendedAction("请先咨询医生。");
        when(drugContraindicationMapper.selectActiveRules(1L)).thenReturn(List.of(contraindication));

        PatientHealthProfile healthProfile = new PatientHealthProfile();
        healthProfile.setLayer2MedicalHistory("患者既往有血管性水肿史");
        when(patientHealthProfileMapper.selectByPatientId(1L, 201L)).thenReturn(healthProfile);

        AppDdiScreenRequest request = new AppDdiScreenRequest();
        request.setNewDrugNames(List.of("依那普利片"));

        AppDdiScreenResult result = appDdiScreeningService.screenForSubject("13800138000", request);

        assertThat(result.getAlerts()).hasSize(1);
        assertThat(result.getAlerts().get(0).getType()).isEqualTo("CONTRAINDICATION");
        assertThat(result.getAlerts().get(0).getDrugB()).isEqualTo("依那普利片");
    }

    @Test
    void shouldRejectEmptyDrugList() {
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());

        AppDdiScreenRequest request = new AppDdiScreenRequest();
        request.setNewDrugNames(Arrays.asList(" ", null));

        assertThatThrownBy(() -> appDdiScreeningService.screenForSubject("13800138000", request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("BAD_REQUEST");
    }

    private AppPatientService.AccessContext accessContext() {
        AppAccount account = new AppAccount();
        account.setId(101L);
        account.setMobile("13800138000");
        account.setAccountType("PATIENT");

        Patient patient = new Patient();
        patient.setId(201L);
        patient.setTenantId(1L);

        return new AppPatientService.AccessContext(account, patient, "PATIENT");
    }
}
