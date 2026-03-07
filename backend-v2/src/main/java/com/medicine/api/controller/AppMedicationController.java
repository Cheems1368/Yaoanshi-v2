package com.medicine.api.controller;

import com.medicine.api.dto.medication.CheckinCorrectRequest;
import com.medicine.api.dto.medication.CheckinRecordDTO;
import com.medicine.api.dto.medication.CheckinSubmitRequest;
import com.medicine.api.dto.medication.DoseTaskDTO;
import com.medicine.api.dto.medication.MedicationPlanDTO;
import com.medicine.common.model.CommonResult;
import com.medicine.common.model.PageResult;
import com.medicine.service.app.AppMedicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class AppMedicationController {

    private final AppMedicationService appMedicationService;

    public AppMedicationController(AppMedicationService appMedicationService) {
        this.appMedicationService = appMedicationService;
    }

    @GetMapping("/app-api/medication-plans/active")
    public CommonResult<MedicationPlanDTO> getActivePlan(Principal principal) {
        return CommonResult.ok(appMedicationService.getActivePlan(principal.getName()));
    }

    @GetMapping("/app-api/dose-tasks/today")
    public CommonResult<List<DoseTaskDTO>> getTodayDoseTasks(
            Principal principal,
            @RequestParam(value = "date", required = false) String date
    ) {
        return CommonResult.ok(appMedicationService.getTodayDoseTasks(principal.getName(), date));
    }

    @GetMapping("/app-api/checkins")
    public CommonResult<PageResult<CheckinRecordDTO>> getCheckins(
            Principal principal,
            @RequestParam(value = "dateFrom", required = false) String dateFrom,
            @RequestParam(value = "dateTo", required = false) String dateTo,
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
    ) {
        return CommonResult.ok(appMedicationService.getCheckins(principal.getName(), dateFrom, dateTo, pageNo, pageSize));
    }

    @PostMapping("/app-api/checkins")
    public CommonResult<CheckinRecordDTO> submitCheckin(Principal principal, @RequestBody CheckinSubmitRequest request) {
        return CommonResult.ok(appMedicationService.submitCheckin(principal.getName(), request));
    }

    @PutMapping("/app-api/checkins/{id}/correct")
    public CommonResult<CheckinRecordDTO> correctCheckin(
            Principal principal,
            @PathVariable("id") Long id,
            @RequestBody CheckinCorrectRequest request
    ) {
        return CommonResult.ok(appMedicationService.correctCheckin(principal.getName(), id, request));
    }
}
