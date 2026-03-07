package com.medicine.service.app;

import com.medicine.api.dto.assessment.KnowledgeAssessmentDTO;
import com.medicine.api.dto.assessment.SatisfactionSurveyDTO;
import com.medicine.common.exception.BusinessException;
import com.medicine.common.exception.ErrorCode;
import com.medicine.domain.assessment.KnowledgeAssessment;
import com.medicine.domain.assessment.KnowledgeAssessmentAnswer;
import com.medicine.domain.assessment.SatisfactionSurvey;
import com.medicine.domain.helporder.HelpOrder;
import com.medicine.repository.assessment.KnowledgeAssessmentAnswerMapper;
import com.medicine.repository.assessment.KnowledgeAssessmentMapper;
import com.medicine.repository.assessment.SatisfactionSurveyMapper;
import com.medicine.repository.helporder.HelpOrderMapper;
import com.medicine.service.auth.AppPatientService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class AppAssessmentService {

    private static final long DEFAULT_TENANT_ID = 1L;

    private final KnowledgeAssessmentMapper knowledgeAssessmentMapper;
    private final KnowledgeAssessmentAnswerMapper knowledgeAssessmentAnswerMapper;
    private final SatisfactionSurveyMapper satisfactionSurveyMapper;
    private final HelpOrderMapper helpOrderMapper;
    private final AppPatientService appPatientService;

    public AppAssessmentService(
            KnowledgeAssessmentMapper knowledgeAssessmentMapper,
            KnowledgeAssessmentAnswerMapper knowledgeAssessmentAnswerMapper,
            SatisfactionSurveyMapper satisfactionSurveyMapper,
            HelpOrderMapper helpOrderMapper,
            AppPatientService appPatientService
    ) {
        this.knowledgeAssessmentMapper = knowledgeAssessmentMapper;
        this.knowledgeAssessmentAnswerMapper = knowledgeAssessmentAnswerMapper;
        this.satisfactionSurveyMapper = satisfactionSurveyMapper;
        this.helpOrderMapper = helpOrderMapper;
        this.appPatientService = appPatientService;
    }

    public KnowledgeAssessmentDTO submitKnowledge(String subject, KnowledgeAssessmentDTO request) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<KnowledgeAssessmentDTO.AnswerDTO> answers = request.getAnswers() == null ? List.of() : request.getAnswers();
        long correctCount = answers.stream().filter(answer -> Boolean.TRUE.equals(answer.getCorrect())).count();

        KnowledgeAssessment assessment = new KnowledgeAssessment();
        assessment.setPatientId(context.patient().getId());
        assessment.setPhase(StringUtils.hasText(request.getPhase()) ? request.getPhase().trim().toUpperCase() : "FOLLOW_UP");
        assessment.setQuestionSetVersion(StringUtils.hasText(request.getQuestionSetVersion()) ? request.getQuestionSetVersion().trim() : "v1");
        assessment.setScore(BigDecimal.valueOf(correctCount));
        assessment.setMaxScore(BigDecimal.valueOf(answers.size()));
        assessment.setSubmittedByType(context.operatorType());
        assessment.setSubmittedById(context.appAccount().getId());
        assessment.setSubmittedAt(now);
        applyAudit(assessment, "app-assessment", now);
        knowledgeAssessmentMapper.insert(assessment);

        for (KnowledgeAssessmentDTO.AnswerDTO answerDto : answers) {
            KnowledgeAssessmentAnswer answer = new KnowledgeAssessmentAnswer();
            answer.setAssessmentId(assessment.getId());
            answer.setQuestionCode(requireText(answerDto.getQuestionCode()));
            answer.setQuestionTextSnapshot(requireText(answerDto.getQuestionText()));
            answer.setSelectedOption(requireText(answerDto.getSelectedOption()));
            answer.setCorrectOption(blankToNull(answerDto.getCorrectOption()));
            answer.setIsCorrect(Boolean.TRUE.equals(answerDto.getCorrect()));
            applyAudit(answer, "app-assessment", now);
            knowledgeAssessmentAnswerMapper.insert(answer);
        }

        KnowledgeAssessmentDTO response = new KnowledgeAssessmentDTO();
        response.setId(assessment.getId());
        response.setPhase(assessment.getPhase());
        response.setQuestionSetVersion(assessment.getQuestionSetVersion());
        response.setScore(assessment.getScore().doubleValue());
        response.setMaxScore(assessment.getMaxScore().doubleValue());
        response.setSubmittedAt(formatDateTime(assessment.getSubmittedAt()));
        response.setAnswers(answers);
        return response;
    }

    public SatisfactionSurveyDTO submitSatisfaction(String subject, SatisfactionSurveyDTO request) {
        if (request == null || request.getHelpOrderId() == null) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        HelpOrder order = helpOrderMapper.selectById(DEFAULT_TENANT_ID, request.getHelpOrderId());
        if (order == null || !context.patient().getId().equals(order.getPatientId())) {
            throw new BusinessException(ErrorCode.HELP_ORDER_NOT_FOUND.getCode(), ErrorCode.HELP_ORDER_NOT_FOUND.getMessage());
        }
        if (!"CLOSED".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.HELP_ORDER_SURVEY_CONFLICT.getCode(), ErrorCode.HELP_ORDER_SURVEY_CONFLICT.getMessage());
        }
        if (satisfactionSurveyMapper.selectByHelpOrderId(DEFAULT_TENANT_ID, order.getId()) != null) {
            throw new BusinessException(ErrorCode.HELP_ORDER_SURVEY_CONFLICT.getCode(), ErrorCode.HELP_ORDER_SURVEY_CONFLICT.getMessage());
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new BusinessException(400, "BAD_REQUEST");
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        SatisfactionSurvey survey = new SatisfactionSurvey();
        survey.setHelpOrderId(order.getId());
        survey.setPatientId(order.getPatientId());
        survey.setRating(request.getRating());
        survey.setComment(blankToNull(request.getComment()));
        survey.setSubmittedByType(context.operatorType());
        survey.setSubmittedById(context.appAccount().getId());
        survey.setSubmittedAt(now);
        applyAudit(survey, "app-assessment", now);
        satisfactionSurveyMapper.insert(survey);

        SatisfactionSurveyDTO response = new SatisfactionSurveyDTO();
        response.setId(survey.getId());
        response.setHelpOrderId(survey.getHelpOrderId());
        response.setRating(survey.getRating());
        response.setComment(survey.getComment());
        response.setSubmittedAt(formatDateTime(survey.getSubmittedAt()));
        return response;
    }

    private String requireText(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(400, "BAD_REQUEST");
        }
        return value.trim();
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC).toString();
    }

    private void applyAudit(com.medicine.domain.common.BaseEntity entity, String operator, LocalDateTime now) {
        entity.setCreator(operator);
        entity.setCreateTime(now);
        entity.setUpdater(operator);
        entity.setUpdateTime(now);
        entity.setDeleted(false);
        entity.setTenantId(DEFAULT_TENANT_ID);
    }
}
