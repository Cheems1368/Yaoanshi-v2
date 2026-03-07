package com.sqlapk.medicine.module.medication.controller;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageParam;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.medication.api.MedPlanService;
import com.sqlapk.medicine.module.medication.api.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 用药计划控制器
 *
 * @author sqlapk
 */
@Tag(name = "管理后台 - 用药计划")
@RestController
@RequestMapping("/medication/med-plan")
@Validated
@Slf4j
public class MedPlanController {

    @Autowired
    private MedPlanService medPlanService;

    @PostMapping("/create")
    @Operation(summary = "创建用药计划")
    @PreAuthorize("@ss.hasPermission('medication:med-plan:create')")
    public CommonResult<Long> createMedPlan(@Valid @RequestBody MedPlanCreateReqDTO createReqDTO) {
        return CommonResult.success(medPlanService.createMedPlan(createReqDTO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用药计划")
    @PreAuthorize("@ss.hasPermission('medication:med-plan:update')")
    public CommonResult<Boolean> updateMedPlan(@Valid @RequestBody MedPlanUpdateReqDTO updateReqDTO) {
        medPlanService.updateMedPlan(updateReqDTO);
        return CommonResult.success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取用药计划")
    @Parameter(name = "id", description = "计划ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:med-plan:query')")
    public CommonResult<MedPlanRespDTO> getMedPlan(@RequestParam("id") Long id) {
        MedPlanRespDTO medPlan = medPlanService.getMedPlan(id);
        return CommonResult.success(medPlan);
    }

    @GetMapping("/list-by-patient")
    @Operation(summary = "根据患者ID获取用药计划列表")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:med-plan:query')")
    public CommonResult<List<MedPlanRespDTO>> getMedPlansByPatientId(@RequestParam("patientId") Long patientId) {
        List<MedPlanRespDTO> medPlans = medPlanService.getMedPlansByPatientId(patientId);
        return CommonResult.success(medPlans);
    }

    @PostMapping("/pause")
    @Operation(summary = "暂停用药计划")
    @Parameter(name = "id", description = "计划ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:med-plan:update')")
    public CommonResult<Boolean> pauseMedPlan(@RequestParam("id") Long id) {
        medPlanService.pauseMedPlan(id);
        return CommonResult.success(true);
    }

    @PostMapping("/resume")
    @Operation(summary = "恢复用药计划")
    @Parameter(name = "id", description = "计划ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:med-plan:update')")
    public CommonResult<Boolean> resumeMedPlan(@RequestParam("id") Long id) {
        medPlanService.resumeMedPlan(id);
        return CommonResult.success(true);
    }

    @PostMapping("/terminate")
    @Operation(summary = "终止用药计划")
    @Parameter(name = "id", description = "计划ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:med-plan:update')")
    public CommonResult<Boolean> terminateMedPlan(@RequestParam("id") Long id) {
        medPlanService.terminateMedPlan(id);
        return CommonResult.success(true);
    }

}
