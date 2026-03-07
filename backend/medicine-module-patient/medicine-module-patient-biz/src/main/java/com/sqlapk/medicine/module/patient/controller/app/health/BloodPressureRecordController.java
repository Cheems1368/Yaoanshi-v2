package com.sqlapk.medicine.module.patient.controller.app.health;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.security.core.annotations.PreAuthenticated;
import com.sqlapk.medicine.module.patient.api.BloodPressureRecordApi;
import com.sqlapk.medicine.module.patient.api.dto.health.BloodPressureRecordReqDTO;
import com.sqlapk.medicine.module.patient.api.dto.health.BloodPressureRecordRespDTO;
import com.sqlapk.medicine.module.patient.api.dto.health.BloodPressureTrendRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;

/**
 * 血压记录控制器
 *
 * @author sqlapk
 */
@Tag(name = "用户 App - 血压记录")
@RestController
@RequestMapping("/app/blood-pressure")
@Validated
@Slf4j
public class BloodPressureRecordController {

    @Autowired
    private BloodPressureRecordApi bloodPressureRecordApi;

    @PostMapping("/create")
    @Operation(summary = "创建血压记录")
    @PreAuthenticated
    public CommonResult<Long> createBloodPressureRecord(@Valid @RequestBody BloodPressureRecordReqDTO reqDTO) {
        return success(bloodPressureRecordApi.createBloodPressureRecord(reqDTO));
    }

    @GetMapping("/recent")
    @Operation(summary = "获取最近血压记录")
    @PreAuthenticated
    public CommonResult<List<BloodPressureRecordRespDTO>> getRecentBloodPressureRecords(
            @Parameter(description = "患者ID", required = true) @RequestParam("patientId") Long patientId,
            @Parameter(description = "记录数量限制", example = "10") @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return success(bloodPressureRecordApi.getRecentBloodPressureRecords(patientId, limit));
    }

    @GetMapping("/trend")
    @Operation(summary = "获取血压趋势分析")
    @PreAuthenticated
    public CommonResult<BloodPressureTrendRespDTO> getBloodPressureTrend(
            @Parameter(description = "患者ID", required = true) @RequestParam("patientId") Long patientId,
            @Parameter(description = "统计天数", example = "7") @RequestParam(value = "days", defaultValue = "7") Integer days) {
        return success(bloodPressureRecordApi.getBloodPressureTrend(patientId, days));
    }

    @GetMapping("/stats")
    @Operation(summary = "获取血压统计信息")
    @PreAuthenticated
    public CommonResult<BloodPressureTrendRespDTO> getBloodPressureStats(
            @Parameter(description = "患者ID", required = true) @RequestParam("patientId") Long patientId,
            @Parameter(description = "统计天数", example = "3") @RequestParam(value = "days", defaultValue = "3") Integer days) {
        return success(bloodPressureRecordApi.getBloodPressureStats(patientId, days));
    }

    @PostMapping("/batch-create")
    @Operation(summary = "批量创建血压记录")
    @PreAuthenticated
    public CommonResult<List<Long>> batchCreateBloodPressureRecords(@Valid @RequestBody List<BloodPressureRecordReqDTO> reqDTOList) {
        return success(bloodPressureRecordApi.batchCreateBloodPressureRecords(reqDTOList));
    }
}
