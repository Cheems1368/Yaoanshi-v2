package com.medicine.service.app;

import com.medicine.api.dto.assessment.SatisfactionSurveyDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.domain.assessment.SatisfactionSurvey;
import com.medicine.domain.helporder.HelpOrder;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.Patient;
import com.medicine.repository.assessment.KnowledgeAssessmentAnswerMapper;
import com.medicine.repository.assessment.KnowledgeAssessmentMapper;
import com.medicine.repository.assessment.SatisfactionSurveyMapper;
import com.medicine.repository.helporder.HelpOrderMapper;
import com.medicine.service.auth.AppPatientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppAssessmentServiceTest {

    @Mock
    private KnowledgeAssessmentMapper knowledgeAssessmentMapper;

    @Mock
    private KnowledgeAssessmentAnswerMapper knowledgeAssessmentAnswerMapper;

    @Mock
    private SatisfactionSurveyMapper satisfactionSurveyMapper;

    @Mock
    private HelpOrderMapper helpOrderMapper;

    @Mock
    private AppPatientService appPatientService;

    @InjectMocks
    private AppAssessmentService appAssessmentService;

    @Test
    void shouldRejectSatisfactionWhenOrderNotClosed() {
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());
        when(helpOrderMapper.selectById(1L, 801L)).thenReturn(helpOrder("PHARMACIST_IN_PROGRESS"));

        assertThatThrownBy(() -> appAssessmentService.submitSatisfaction("13800138000", request()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("HELP_ORDER_SURVEY_CONFLICT");
    }

    @Test
    void shouldRejectDuplicateSatisfactionForClosedOrder() {
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());
        when(helpOrderMapper.selectById(1L, 801L)).thenReturn(helpOrder("CLOSED"));
        when(satisfactionSurveyMapper.selectByHelpOrderId(1L, 801L)).thenReturn(new SatisfactionSurvey());

        assertThatThrownBy(() -> appAssessmentService.submitSatisfaction("13800138000", request()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("HELP_ORDER_SURVEY_CONFLICT");
    }

    @Test
    void shouldPersistSatisfactionOnceForClosedOrder() {
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());
        when(helpOrderMapper.selectById(1L, 801L)).thenReturn(helpOrder("CLOSED"));
        when(satisfactionSurveyMapper.selectByHelpOrderId(1L, 801L)).thenReturn(null);
        doAnswer(invocation -> {
            SatisfactionSurvey survey = invocation.getArgument(0);
            survey.setId(901L);
            return 1;
        }).when(satisfactionSurveyMapper).insert(any(SatisfactionSurvey.class));

        SatisfactionSurveyDTO result = appAssessmentService.submitSatisfaction("13800138000", request());

        verify(satisfactionSurveyMapper).insert(any(SatisfactionSurvey.class));
        assertThat(result.getId()).isEqualTo(901L);
        assertThat(result.getHelpOrderId()).isEqualTo(801L);
        assertThat(result.getRating()).isEqualTo(5);
    }

    private AppPatientService.AccessContext accessContext() {
        AppAccount account = new AppAccount();
        account.setId(101L);
        account.setMobile("13800138000");
        account.setAccountType("PATIENT");

        Patient patient = new Patient();
        patient.setId(201L);
        patient.setAppAccountId(101L);

        return new AppPatientService.AccessContext(account, patient, "PATIENT");
    }

    private HelpOrder helpOrder(String status) {
        HelpOrder order = new HelpOrder();
        order.setId(801L);
        order.setPatientId(201L);
        order.setStatus(status);
        return order;
    }

    private SatisfactionSurveyDTO request() {
        SatisfactionSurveyDTO request = new SatisfactionSurveyDTO();
        request.setHelpOrderId(801L);
        request.setRating(5);
        request.setComment("good");
        return request;
    }
}
