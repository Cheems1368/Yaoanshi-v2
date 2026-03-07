package com.medicine.api.controller;

import com.medicine.api.dto.assessment.KnowledgeAssessmentDTO;
import com.medicine.api.dto.assessment.SatisfactionSurveyDTO;
import com.medicine.common.model.CommonResult;
import com.medicine.service.app.AppAssessmentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/app-api/assessments")
public class AppAssessmentController {

    private final AppAssessmentService appAssessmentService;

    public AppAssessmentController(AppAssessmentService appAssessmentService) {
        this.appAssessmentService = appAssessmentService;
    }

    @PostMapping("/knowledge")
    public CommonResult<KnowledgeAssessmentDTO> submitKnowledge(@RequestBody KnowledgeAssessmentDTO request, Principal principal) {
        return CommonResult.ok(appAssessmentService.submitKnowledge(principal.getName(), request));
    }

    @PostMapping("/satisfaction")
    public CommonResult<SatisfactionSurveyDTO> submitSatisfaction(@RequestBody SatisfactionSurveyDTO request, Principal principal) {
        return CommonResult.ok(appAssessmentService.submitSatisfaction(principal.getName(), request));
    }
}
