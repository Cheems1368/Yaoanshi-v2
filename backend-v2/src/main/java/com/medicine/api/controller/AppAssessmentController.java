package com.medicine.api.controller;

import com.medicine.api.dto.assessment.KnowledgeAssessmentDTO;
import com.medicine.api.dto.assessment.KnowledgeAssessmentStatusDTO;
import com.medicine.api.dto.assessment.KnowledgeAssessmentSubmitRequest;
import com.medicine.api.dto.assessment.KnowledgeQuestionDTO;
import com.medicine.api.dto.assessment.SatisfactionStatusDTO;
import com.medicine.api.dto.assessment.SatisfactionSurveyDTO;
import com.medicine.common.model.CommonResult;
import com.medicine.service.app.AppAssessmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/app-api/assessments")
public class AppAssessmentController {

    private final AppAssessmentService appAssessmentService;

    public AppAssessmentController(AppAssessmentService appAssessmentService) {
        this.appAssessmentService = appAssessmentService;
    }

    @GetMapping("/knowledge/questions")
    public CommonResult<List<KnowledgeQuestionDTO>> knowledgeQuestions() {
        return CommonResult.ok(appAssessmentService.getKnowledgeQuestions());
    }

    @GetMapping("/knowledge/status")
    public CommonResult<KnowledgeAssessmentStatusDTO> knowledgeStatus(Principal principal) {
        return CommonResult.ok(appAssessmentService.getKnowledgeStatus(principal.getName()));
    }

    @PostMapping("/knowledge")
    public CommonResult<KnowledgeAssessmentDTO> submitKnowledge(@RequestBody KnowledgeAssessmentSubmitRequest request, Principal principal) {
        return CommonResult.ok(appAssessmentService.submitKnowledge(principal.getName(), request));
    }

    @GetMapping("/satisfaction/status")
    public CommonResult<SatisfactionStatusDTO> satisfactionStatus(@RequestParam("helpOrderId") Long helpOrderId, Principal principal) {
        return CommonResult.ok(appAssessmentService.getSatisfactionStatus(principal.getName(), helpOrderId));
    }

    @PostMapping("/satisfaction")
    public CommonResult<SatisfactionSurveyDTO> submitSatisfaction(@RequestBody SatisfactionSurveyDTO request, Principal principal) {
        return CommonResult.ok(appAssessmentService.submitSatisfaction(principal.getName(), request));
    }
}
