package com.sqlapk.medicine.module.patient.controller.app.patient;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.security.core.annotations.PreAuthenticated;
import com.sqlapk.medicine.module.patient.controller.app.patient.vo.AppPatientProfileRespVO;
import com.sqlapk.medicine.module.patient.controller.app.patient.vo.AppPatientProfileSaveReqVO;
import com.sqlapk.medicine.module.patient.service.patient.AppPatientProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;
import static com.sqlapk.medicine.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * App 端患者个人档案接口
 */
@Tag(name = "用户 App - 患者档案")
@RestController
@RequestMapping("/app/patient")
@Validated
public class AppPatientProfileController {

    @Resource
    private AppPatientProfileService appPatientProfileService;

    @PostMapping("/profile")
    @Operation(summary = "保存患者档案")
    @PreAuthenticated
    public CommonResult<Long> saveProfile(@Valid @RequestBody AppPatientProfileSaveReqVO reqVO) {
        Long userId = getLoginUserId();
        Long patientId = appPatientProfileService.saveProfile(userId, reqVO);
        return success(patientId);
    }

    @GetMapping("/profile")
    @Operation(summary = "获取患者档案")
    @PreAuthenticated
    public CommonResult<AppPatientProfileRespVO> getProfile(@RequestParam(value = "patientId", required = false) Long patientId) {
        Long userId = getLoginUserId();
        AppPatientProfileRespVO respVO = appPatientProfileService.getProfile(userId, patientId);
        return success(respVO);
    }
}
