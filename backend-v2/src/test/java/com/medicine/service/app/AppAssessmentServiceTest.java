package com.medicine.service.app;

import com.medicine.api.dto.assessment.SatisfactionSurveyDTO;
import com.medicine.api.dto.assessment.KnowledgeAssessmentSubmitRequest;
import com.medicine.common.exception.BusinessException;
import com.medicine.domain.assessment.SatisfactionSurvey;
import com.medicine.domain.assessment.KnowledgeAssessment;
import com.medicine.domain.helporder.HelpOrder;
import com.medicine.domain.identity.AppAccount;
import com.medicine.domain.identity.Patient;
import com.medicine.repository.assessment.KnowledgeAssessmentAnswerMapper;
import com.medicine.repository.assessment.KnowledgeAssessmentMapper;
import com.medicine.repository.assessment.SatisfactionSurveyMapper;
import com.medicine.repository.helporder.HelpOrderMapper;
import com.medicine.repository.medication.MedicationPlanMapper;
import com.medicine.service.auth.AppPatientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
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
    private MedicationPlanMapper medicationPlanMapper;

    @Mock
    private AppPatientService appPatientService;

    @Spy
    private KnowledgeQuestionBank questionBank = new KnowledgeQuestionBank();

    @InjectMocks
    private AppAssessmentService appAssessmentService;

    @Test
    void shouldScoreKnowledgeAnswersFromServerQuestionBank() {
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());
        when(medicationPlanMapper.selectFirstActivatedAt(1L, 201L)).thenReturn(LocalDateTime.now().minusDays(2));
        when(knowledgeAssessmentMapper.selectByPatientPhaseAndVersion(1L, 201L, "DAY_1", KnowledgeQuestionBank.VERSION)).thenReturn(null);
        when(knowledgeAssessmentMapper.selectByPatientPhaseAndVersion(1L, 201L, "WEEK_4", KnowledgeQuestionBank.VERSION)).thenReturn(null);
        doAnswer(invocation -> {
            KnowledgeAssessment assessment = invocation.getArgument(0);
            assessment.setId(801L);
            return 1;
        }).when(knowledgeAssessmentMapper).insert(any(KnowledgeAssessment.class));

        KnowledgeAssessmentSubmitRequest request = knowledgeRequest("DAY_1");
        request.getAnswers().get(9).setSelectedOption("A");

        var result = appAssessmentService.submitKnowledge("13800138000", request);

        assertThat(result.getScore()).isEqualTo(9.0);
        assertThat(result.getMaxScore()).isEqualTo(10.0);
        assertThat(result.getAnswers().get(9).getCorrect()).isFalse();
        verify(knowledgeAssessmentAnswerMapper, org.mockito.Mockito.times(10)).insert(any());
    }

    @Test
    void shouldNotExposeAssessmentBeforeFirstPlanStartDate() {
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());
        when(medicationPlanMapper.selectFirstActivatedAt(1L, 201L))
                .thenReturn(LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay());

        var result = appAssessmentService.getKnowledgeStatus("13800138000");

        assertThat(result.isDue()).isFalse();
        assertThat(result.getCurrentPhase()).isNull();
        assertThat(result.getPlanStartDate()).isEqualTo(LocalDate.now(ZoneOffset.UTC).plusDays(1).toString());
    }

    @Test
    void shouldRejectAlreadySubmittedCurrentPhase() {
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());
        when(medicationPlanMapper.selectFirstActivatedAt(1L, 201L)).thenReturn(LocalDateTime.now().minusDays(2));
        KnowledgeAssessment existing = new KnowledgeAssessment();
        existing.setScore(BigDecimal.TEN);
        existing.setMaxScore(BigDecimal.TEN);
        when(knowledgeAssessmentMapper.selectByPatientPhaseAndVersion(1L, 201L, "DAY_1", KnowledgeQuestionBank.VERSION)).thenReturn(existing);

        assertThatThrownBy(() -> appAssessmentService.submitKnowledge("13800138000", knowledgeRequest("DAY_1")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("KNOWLEDGE_ASSESSMENT_NOT_DUE");
    }

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

    @Test
    void shouldRejectSatisfactionCommentLongerThanDatabaseLimit() {
        when(appPatientService.requireAccessContext("13800138000")).thenReturn(accessContext());
        when(helpOrderMapper.selectById(1L, 801L)).thenReturn(helpOrder("CLOSED"));
        when(satisfactionSurveyMapper.selectByHelpOrderId(1L, 801L)).thenReturn(null);
        SatisfactionSurveyDTO request = request();
        request.setComment("x".repeat(256));

        assertThatThrownBy(() -> appAssessmentService.submitSatisfaction("13800138000", request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("BAD_REQUEST");
        verify(satisfactionSurveyMapper, never()).insert(any(SatisfactionSurvey.class));
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

    private KnowledgeAssessmentSubmitRequest knowledgeRequest(String phase) {
        KnowledgeAssessmentSubmitRequest request = new KnowledgeAssessmentSubmitRequest();
        request.setPhase(phase);
        request.setQuestionSetVersion(KnowledgeQuestionBank.VERSION);
        request.setAnswers(questionBank.publicQuestions().stream().map(question -> {
            KnowledgeAssessmentSubmitRequest.AnswerRequest answer = new KnowledgeAssessmentSubmitRequest.AnswerRequest();
            answer.setQuestionCode(question.questionCode());
            answer.setSelectedOption(questionBank.find(question.questionCode()).correctOption());
            return answer;
        }).toList());
        return request;
    }
}
