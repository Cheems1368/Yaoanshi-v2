package com.medicine.service.app;

import com.medicine.api.dto.assessment.KnowledgeAssessmentDTO;
import com.medicine.api.dto.assessment.KnowledgeAssessmentStatusDTO;
import com.medicine.api.dto.assessment.KnowledgeAssessmentSubmitRequest;
import com.medicine.api.dto.assessment.KnowledgeQuestionDTO;
import com.medicine.api.dto.assessment.SatisfactionStatusDTO;
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
import com.medicine.repository.medication.MedicationPlanMapper;
import com.medicine.service.auth.AppPatientService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AppAssessmentService {
    private static final long DEFAULT_TENANT_ID = 1L;
    private static final Set<String> PHASES = Set.of("DAY_1", "WEEK_4");

    private final KnowledgeAssessmentMapper knowledgeAssessmentMapper;
    private final KnowledgeAssessmentAnswerMapper knowledgeAssessmentAnswerMapper;
    private final SatisfactionSurveyMapper satisfactionSurveyMapper;
    private final HelpOrderMapper helpOrderMapper;
    private final MedicationPlanMapper medicationPlanMapper;
    private final AppPatientService appPatientService;
    private final KnowledgeQuestionBank questionBank;

    public AppAssessmentService(KnowledgeAssessmentMapper knowledgeAssessmentMapper,
                                KnowledgeAssessmentAnswerMapper knowledgeAssessmentAnswerMapper,
                                SatisfactionSurveyMapper satisfactionSurveyMapper,
                                HelpOrderMapper helpOrderMapper,
                                MedicationPlanMapper medicationPlanMapper,
                                AppPatientService appPatientService,
                                KnowledgeQuestionBank questionBank) {
        this.knowledgeAssessmentMapper = knowledgeAssessmentMapper;
        this.knowledgeAssessmentAnswerMapper = knowledgeAssessmentAnswerMapper;
        this.satisfactionSurveyMapper = satisfactionSurveyMapper;
        this.helpOrderMapper = helpOrderMapper;
        this.medicationPlanMapper = medicationPlanMapper;
        this.appPatientService = appPatientService;
        this.questionBank = questionBank;
    }

    public List<KnowledgeQuestionDTO> getKnowledgeQuestions() { return questionBank.publicQuestions(); }

    public KnowledgeAssessmentStatusDTO getKnowledgeStatus(String subject) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        return buildKnowledgeStatus(context.patient().getId(), LocalDate.now(ZoneOffset.UTC));
    }

    @Transactional
    public KnowledgeAssessmentDTO submitKnowledge(String subject, KnowledgeAssessmentSubmitRequest request) {
        if (request == null || !KnowledgeQuestionBank.VERSION.equals(request.getQuestionSetVersion())) {
            throw new BusinessException(400, "KNOWLEDGE_QUESTION_SET_INVALID");
        }
        String phase = normalizePhase(request.getPhase());
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        KnowledgeAssessmentStatusDTO status = buildKnowledgeStatus(context.patient().getId(), LocalDate.now(ZoneOffset.UTC));
        if (!status.isDue() || !phase.equals(status.getCurrentPhase())) {
            throw new BusinessException(409, "KNOWLEDGE_ASSESSMENT_NOT_DUE");
        }
        List<KnowledgeAssessmentSubmitRequest.AnswerRequest> submitted = request.getAnswers() == null ? List.of() : request.getAnswers();
        if (submitted.size() != questionBank.size()) {
            throw new BusinessException(400, "KNOWLEDGE_ANSWERS_INCOMPLETE");
        }
        Set<String> seen = new HashSet<>();
        List<ValidatedAnswer> answers = submitted.stream().map(answer -> validateAnswer(answer, seen)).toList();
        long correctCount = answers.stream().filter(ValidatedAnswer::correct).count();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        KnowledgeAssessment assessment = new KnowledgeAssessment();
        assessment.setPatientId(context.patient().getId());
        assessment.setPhase(phase);
        assessment.setQuestionSetVersion(KnowledgeQuestionBank.VERSION);
        assessment.setScore(BigDecimal.valueOf(correctCount));
        assessment.setMaxScore(BigDecimal.valueOf(questionBank.size()));
        assessment.setSubmittedByType(context.operatorType());
        assessment.setSubmittedById(context.appAccount().getId());
        assessment.setSubmittedAt(now);
        applyAudit(assessment, "app-assessment", now);
        try {
            knowledgeAssessmentMapper.insert(assessment);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(409, "KNOWLEDGE_ASSESSMENT_ALREADY_SUBMITTED");
        }
        for (ValidatedAnswer validated : answers) {
            KnowledgeAssessmentAnswer answer = new KnowledgeAssessmentAnswer();
            answer.setAssessmentId(assessment.getId());
            answer.setQuestionCode(validated.question().code());
            answer.setQuestionTextSnapshot(validated.question().text());
            answer.setSelectedOption(validated.selectedOption());
            answer.setCorrectOption(validated.question().correctOption());
            answer.setIsCorrect(validated.correct());
            applyAudit(answer, "app-assessment", now);
            knowledgeAssessmentAnswerMapper.insert(answer);
        }
        KnowledgeAssessmentDTO response = toKnowledgeDto(assessment);
        response.setAnswers(answers.stream().map(this::toAnswerDto).toList());
        return response;
    }

    public SatisfactionStatusDTO getSatisfactionStatus(String subject, Long helpOrderId) {
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        HelpOrder order = requireOwnedHelpOrder(context, helpOrderId);
        SatisfactionSurvey survey = satisfactionSurveyMapper.selectByHelpOrderId(DEFAULT_TENANT_ID, order.getId());
        SatisfactionStatusDTO status = new SatisfactionStatusDTO();
        status.setHelpOrderId(order.getId());
        status.setEligible("CLOSED".equals(order.getStatus()) && survey == null);
        status.setSubmitted(survey != null);
        status.setSurvey(toSatisfactionDto(survey));
        return status;
    }

    @Transactional
    public SatisfactionSurveyDTO submitSatisfaction(String subject, SatisfactionSurveyDTO request) {
        if (request == null || request.getHelpOrderId() == null) throw new BusinessException(400, "BAD_REQUEST");
        AppPatientService.AccessContext context = appPatientService.requireAccessContext(subject);
        HelpOrder order = requireOwnedHelpOrder(context, request.getHelpOrderId());
        if (!"CLOSED".equals(order.getStatus()) || satisfactionSurveyMapper.selectByHelpOrderId(DEFAULT_TENANT_ID, order.getId()) != null) {
            throw new BusinessException(ErrorCode.HELP_ORDER_SURVEY_CONFLICT.getCode(), ErrorCode.HELP_ORDER_SURVEY_CONFLICT.getMessage());
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) throw new BusinessException(400, "BAD_REQUEST");
        if (request.getComment() != null && request.getComment().length() > 255) throw new BusinessException(400, "BAD_REQUEST");
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
        return toSatisfactionDto(survey);
    }

    private KnowledgeAssessmentStatusDTO buildKnowledgeStatus(Long patientId, LocalDate today) {
        LocalDateTime firstActivatedAt = medicationPlanMapper.selectFirstActivatedAt(DEFAULT_TENANT_ID, patientId);
        KnowledgeAssessment day1 = knowledgeAssessmentMapper.selectByPatientPhaseAndVersion(DEFAULT_TENANT_ID, patientId, "DAY_1", KnowledgeQuestionBank.VERSION);
        KnowledgeAssessment week4 = knowledgeAssessmentMapper.selectByPatientPhaseAndVersion(DEFAULT_TENANT_ID, patientId, "WEEK_4", KnowledgeQuestionBank.VERSION);
        KnowledgeAssessmentStatusDTO status = new KnowledgeAssessmentStatusDTO();
        status.setQuestionSetVersion(KnowledgeQuestionBank.VERSION);
        status.setDay1(toKnowledgeDto(day1));
        status.setWeek4(toKnowledgeDto(week4));
        if (firstActivatedAt == null) return status;
        LocalDate startDate = firstActivatedAt.toLocalDate();
        if (today.isBefore(startDate)) {
            status.setPlanStartDate(startDate.toString());
            return status;
        }
        String currentPhase = ChronoUnit.DAYS.between(startDate, today) >= 27 ? "WEEK_4" : "DAY_1";
        status.setPlanStartDate(startDate.toString());
        status.setCurrentPhase(currentPhase);
        status.setDue("WEEK_4".equals(currentPhase) ? week4 == null : day1 == null);
        return status;
    }

    private ValidatedAnswer validateAnswer(KnowledgeAssessmentSubmitRequest.AnswerRequest answer, Set<String> seen) {
        if (answer == null || !StringUtils.hasText(answer.getQuestionCode()) || !StringUtils.hasText(answer.getSelectedOption())) {
            throw new BusinessException(400, "KNOWLEDGE_ANSWERS_INVALID");
        }
        String code = answer.getQuestionCode().trim();
        String selected = answer.getSelectedOption().trim().toUpperCase();
        KnowledgeQuestionBank.Question question = questionBank.find(code);
        if (question == null || !question.hasOption(selected) || !seen.add(code)) throw new BusinessException(400, "KNOWLEDGE_ANSWERS_INVALID");
        return new ValidatedAnswer(question, selected, question.correctOption().equals(selected));
    }

    private String normalizePhase(String value) {
        String phase = StringUtils.hasText(value) ? value.trim().toUpperCase() : "";
        if (!PHASES.contains(phase)) throw new BusinessException(400, "KNOWLEDGE_PHASE_INVALID");
        return phase;
    }

    private HelpOrder requireOwnedHelpOrder(AppPatientService.AccessContext context, Long id) {
        HelpOrder order = id == null ? null : helpOrderMapper.selectById(DEFAULT_TENANT_ID, id);
        if (order == null || !context.patient().getId().equals(order.getPatientId())) {
            throw new BusinessException(ErrorCode.HELP_ORDER_NOT_FOUND.getCode(), ErrorCode.HELP_ORDER_NOT_FOUND.getMessage());
        }
        return order;
    }

    private KnowledgeAssessmentDTO toKnowledgeDto(KnowledgeAssessment assessment) {
        if (assessment == null) return null;
        KnowledgeAssessmentDTO dto = new KnowledgeAssessmentDTO();
        dto.setId(assessment.getId()); dto.setPhase(assessment.getPhase()); dto.setQuestionSetVersion(assessment.getQuestionSetVersion());
        dto.setScore(assessment.getScore().doubleValue()); dto.setMaxScore(assessment.getMaxScore().doubleValue());
        dto.setSubmittedAt(formatDateTime(assessment.getSubmittedAt()));
        return dto;
    }

    private KnowledgeAssessmentDTO.AnswerDTO toAnswerDto(ValidatedAnswer validated) {
        KnowledgeAssessmentDTO.AnswerDTO dto = new KnowledgeAssessmentDTO.AnswerDTO();
        dto.setQuestionCode(validated.question().code()); dto.setQuestionText(validated.question().text());
        dto.setSelectedOption(validated.selectedOption()); dto.setCorrectOption(validated.question().correctOption()); dto.setCorrect(validated.correct());
        return dto;
    }

    private SatisfactionSurveyDTO toSatisfactionDto(SatisfactionSurvey survey) {
        if (survey == null) return null;
        SatisfactionSurveyDTO dto = new SatisfactionSurveyDTO();
        dto.setId(survey.getId()); dto.setHelpOrderId(survey.getHelpOrderId()); dto.setRating(survey.getRating());
        dto.setComment(survey.getComment()); dto.setSubmittedAt(formatDateTime(survey.getSubmittedAt()));
        return dto;
    }

    private String blankToNull(String value) { return StringUtils.hasText(value) ? value.trim() : null; }
    private String formatDateTime(LocalDateTime value) { return value == null ? null : value.toInstant(ZoneOffset.UTC).toString(); }
    private void applyAudit(com.medicine.domain.common.BaseEntity entity, String operator, LocalDateTime now) {
        entity.setCreator(operator); entity.setCreateTime(now); entity.setUpdater(operator); entity.setUpdateTime(now);
        entity.setDeleted(false); entity.setTenantId(DEFAULT_TENANT_ID);
    }
    private record ValidatedAnswer(KnowledgeQuestionBank.Question question, String selectedOption, boolean correct) { }
}
