package com.medicine.api.controller;

import com.medicine.api.dto.ddi.AdminDdiScreenResult;
import com.medicine.api.dto.medication.DoseTaskDTO;
import com.medicine.api.dto.medication.MedicationPlanDTO;
import com.medicine.api.dto.medication.PlanActivationRequest;
import com.medicine.common.model.CommonResult;
import com.medicine.service.admin.AdminMedicationPlanService;
import com.medicine.service.auth.JwtClaims;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin-api/medication-plans")
public class AdminMedicationPlanController {

    private final AdminMedicationPlanService adminMedicationPlanService;

    public AdminMedicationPlanController(AdminMedicationPlanService adminMedicationPlanService) {
        this.adminMedicationPlanService = adminMedicationPlanService;
    }

    @PostMapping
    public CommonResult<MedicationPlanDTO> createPlan(@RequestBody MedicationPlanDTO request, Authentication authentication) {
        return CommonResult.ok(adminMedicationPlanService.createPlan((JwtClaims) authentication.getDetails(), request));
    }

    @PutMapping("/{id}")
    public CommonResult<MedicationPlanDTO> updatePlan(
            @PathVariable("id") Long id,
            @RequestBody MedicationPlanDTO request,
            Authentication authentication
    ) {
        return CommonResult.ok(adminMedicationPlanService.updatePlan((JwtClaims) authentication.getDetails(), id, request));
    }

    @PostMapping("/{id}/activate")
    public CommonResult<MedicationPlanDTO> activatePlan(@PathVariable("id") Long id,
                                                         @RequestBody(required = false) PlanActivationRequest request,
                                                         Authentication authentication) {
        return CommonResult.ok(adminMedicationPlanService.activatePlan((JwtClaims) authentication.getDetails(), id, request));
    }

    @PostMapping("/{id}/stop")
    public CommonResult<MedicationPlanDTO> stopPlan(@PathVariable("id") Long id, Authentication authentication) {
        return CommonResult.ok(adminMedicationPlanService.stopPlan((JwtClaims) authentication.getDetails(), id));
    }

    @GetMapping("/{id}/activation-conflicts")
    public CommonResult<List<DoseTaskDTO>> getActivationConflicts(@PathVariable("id") Long id, Authentication authentication) {
        return CommonResult.ok(adminMedicationPlanService.getActivationConflicts((JwtClaims) authentication.getDetails(), id));
    }

    @GetMapping("/{id}")
    public CommonResult<MedicationPlanDTO> getPlan(@PathVariable("id") Long id, Authentication authentication) {
        return CommonResult.ok(adminMedicationPlanService.getPlan((JwtClaims) authentication.getDetails(), id));
    }

    @GetMapping("/{id}/tasks")
    public CommonResult<List<DoseTaskDTO>> getPlanTasks(
            @PathVariable("id") Long id,
            @RequestParam(value = "dateFrom", required = false) String dateFrom,
            @RequestParam(value = "dateTo", required = false) String dateTo,
            Authentication authentication
    ) {
        return CommonResult.ok(adminMedicationPlanService.getPlanTasks((JwtClaims) authentication.getDetails(), id, dateFrom, dateTo));
    }

    @GetMapping("/{id}/adherence-report")
    public CommonResult<String> getAdherenceReport(
            @PathVariable("id") Long id,
            @RequestParam(value = "dateFrom", required = false) String dateFrom,
            @RequestParam(value = "dateTo", required = false) String dateTo,
            Authentication authentication
    ) {
        return CommonResult.ok(adminMedicationPlanService.buildAdherenceReport((JwtClaims) authentication.getDetails(), id, dateFrom, dateTo));
    }

    @GetMapping("/{id}/ddi-check")
    public CommonResult<AdminDdiScreenResult> ddiCheck(@PathVariable("id") Long id, Authentication authentication) {
        return CommonResult.ok(adminMedicationPlanService.ddiCheckForPlan((JwtClaims) authentication.getDetails(), id));
    }
}
