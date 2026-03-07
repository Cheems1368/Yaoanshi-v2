package com.sqlapk.medicine.module.medication.controller;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.module.medication.api.TodayTaskService;
import com.sqlapk.medicine.module.medication.api.dto.TodayTaskCompleteReqDTO;
import com.sqlapk.medicine.module.medication.api.dto.TodayTaskRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * 今日待办控制器
 *
 * @author sqlapk
 */
@Tag(name = "管理后台 - 今日待办")
@RestController
@RequestMapping("/medication/today-task")
@Validated
@Slf4j
public class TodayTaskController {

    @Autowired
    private TodayTaskService todayTaskService;

    @GetMapping("/list-by-patient")
    @Operation(summary = "根据患者ID和日期获取今日待办任务")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @Parameter(name = "taskDate", description = "任务日期", required = true)
    @PreAuthorize("@ss.hasPermission('medication:today-task:query')")
    public CommonResult<List<TodayTaskRespDTO>> getTodayTasksByPatientIdAndDate(
            @RequestParam("patientId") Long patientId,
            @RequestParam("taskDate") LocalDate taskDate) {
        List<TodayTaskRespDTO> tasks = todayTaskService.getTodayTasksByPatientIdAndDate(patientId, taskDate);
        return CommonResult.success(tasks);
    }

    @PostMapping("/complete")
    @Operation(summary = "完成待办任务")
    @PreAuthorize("@ss.hasPermission('medication:today-task:update')")
    public CommonResult<Boolean> completeTask(@Valid @RequestBody TodayTaskCompleteReqDTO completeReqDTO) {
        todayTaskService.completeTask(completeReqDTO);
        return CommonResult.success(true);
    }

    @PostMapping("/skip")
    @Operation(summary = "跳过待办任务")
    @Parameter(name = "taskId", description = "任务ID", required = true)
    @Parameter(name = "notes", description = "备注")
    @PreAuthorize("@ss.hasPermission('medication:today-task:update')")
    public CommonResult<Boolean> skipTask(@RequestParam("taskId") Long taskId,
                                           @RequestParam(value = "notes", required = false) String notes) {
        todayTaskService.skipTask(taskId, notes);
        return CommonResult.success(true);
    }

}
