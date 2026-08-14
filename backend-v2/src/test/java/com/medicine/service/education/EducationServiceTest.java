package com.medicine.service.education;

import com.medicine.api.dto.education.EducationContentCardDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.domain.education.EducationContentCard;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.Patient;
import com.medicine.domain.identity.PatientHealthProfile;
import com.medicine.domain.medication.MedicationPlan;
import com.medicine.domain.medication.MedicationPlanItem;
import com.medicine.repository.education.EducationContentCardMapper;
import com.medicine.repository.identity.PatientHealthProfileMapper;
import com.medicine.repository.medication.MedicationPlanItemMapper;
import com.medicine.repository.medication.MedicationPlanMapper;
import com.medicine.service.admin.CsvImportSupport;
import com.medicine.service.auth.AppPatientService;
import com.medicine.service.auth.ProfessionalIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EducationServiceTest {

    @Mock
    private EducationContentCardMapper educationContentCardMapper;

    @Mock
    private AppPatientService appPatientService;

    @Mock
    private PatientHealthProfileMapper patientHealthProfileMapper;

    @Mock
    private MedicationPlanMapper medicationPlanMapper;

    @Mock
    private MedicationPlanItemMapper medicationPlanItemMapper;

    @Mock
    private ProfessionalIdentityService professionalIdentityService;

    @Mock
    private CsvImportSupport csvImportSupport;

    @InjectMocks
    private EducationService educationService;

    @Test
    void shouldRecommendReviewedCardsFromPatientContextWithoutBody() {
        AppAccount account = new AppAccount();
        account.setId(101L);
        account.setAccountType("FAMILY");
        Patient patient = new Patient();
        patient.setId(201L);
        patient.setAge(70);
        patient.setPrimaryDiseaseName("hypertension");
        patient.setPrimaryDiseaseCode("I10");
        PatientHealthProfile healthProfile = new PatientHealthProfile();
        healthProfile.setLayer1Diagnosis("diabetes|CKD");
        MedicationPlan plan = new MedicationPlan();
        plan.setId(301L);
        MedicationPlanItem item = new MedicationPlanItem();
        item.setDrugName("Metformin");
        EducationContentCard card = reviewedCard(401L, "CARD-001", "Hypertension basics");

        when(appPatientService.requireAccessContext("13800138000"))
                .thenReturn(new AppPatientService.AccessContext(account, patient, "FAMILY"));
        when(patientHealthProfileMapper.selectByPatientId(1L, 201L)).thenReturn(healthProfile);
        when(medicationPlanMapper.selectActiveByPatientId(1L, 201L)).thenReturn(plan);
        when(medicationPlanItemMapper.selectByPlanId(1L, 301L)).thenReturn(List.of(item));
        ArgumentCaptor<List<String>> tagsCaptor = ArgumentCaptor.forClass(List.class);
        when(educationContentCardMapper.selectReviewedRecommendations(org.mockito.Mockito.eq(1L), tagsCaptor.capture(), org.mockito.Mockito.eq(5)))
                .thenReturn(List.of(card));

        List<EducationContentCardDTO> result = educationService.recommendations("13800138000", 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Hypertension basics");
        assertThat(result.get(0).getBody()).isNull();
        assertThat(tagsCaptor.getValue()).contains("hypertension", "I10", "diabetes", "CKD", "Metformin", "ELDERLY", "老年", "GENERAL");
    }

    @Test
    void shouldRejectUnreviewedAppDetail() {
        EducationContentCard draft = reviewedCard(401L, "CARD-001", "Draft card");
        draft.setReviewStatus("DRAFT");
        when(educationContentCardMapper.selectById(1L, 401L)).thenReturn(draft);

        assertThatThrownBy(() -> educationService.appDetail(401L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("EDUCATION_CARD_NOT_FOUND");
    }

    private EducationContentCard reviewedCard(Long id, String code, String title) {
        EducationContentCard card = new EducationContentCard();
        card.setId(id);
        card.setContentCode(code);
        card.setTitle(title);
        card.setSummary("Reviewed summary");
        card.setBody("Reviewed body");
        card.setCategory("chronic");
        card.setCategoryName("Chronic disease");
        card.setDiagnosisTags("GENERAL|hypertension");
        card.setDrugTags("Metformin");
        card.setPopulationTags("ELDERLY|老年");
        card.setReviewStatus("REVIEWED");
        card.setReviewedAt(LocalDateTime.of(2026, 5, 14, 8, 0));
        card.setUpdateTime(LocalDateTime.of(2026, 5, 14, 9, 0));
        return card;
    }
}
