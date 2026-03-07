package com.sqlapk.medicine.module.medication.controller.admin;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageParam;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.framework.operatelog.core.enums.OperateTypeEnum;
import com.sqlapk.medicine.module.medication.api.MedCheckinService;
import com.sqlapk.medicine.module.medication.api.dto.*;
import com.sqlapk.medicine.module.medication.controller.admin.vo.CheckinPageReqVO;
import com.sqlapk.medicine.module.medication.controller.admin.vo.CheckinRespVO;
import com.sqlapk.medicine.module.medication.convert.MedCheckinConvert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 用药打卡记录
 *
 * @author sqlapk
 */
@Tag(name = "管理后台 - 用药打卡记录")
@RestController
@RequestMapping("/medication/checkin")
@Validated
@Slf4j
public class MedCheckinController {

    @Autowired
    private MedCheckinService medCheckinService;

    @PostMapping("/submit")
    @Operation(summary = "提交打卡")
    @PreAuthorize("@ss.hasPermission('medication:checkin:submit')")
    @OperateLog(module = "用药打卡系统", name = "提交打卡", type = OperateTypeEnum.CREATE)
    public CommonResult<Long> submitCheckin(@Valid @RequestBody CheckinSubmitReqDTO submitReqDTO) {
        Long checkinId = medCheckinService.submitCheckin(submitReqDTO);
        return success(checkinId);
    }

    @PostMapping("/revoke")
    @Operation(summary = "撤销打卡")
    @PreAuthorize("@ss.hasPermission('medication:checkin:revoke')")
    @OperateLog(module = "用药打卡系统", name = "撤销打卡", type = OperateTypeEnum.UPDATE)
    public CommonResult<Boolean> revokeCheckin(@Valid @RequestBody CheckinRevokeReqDTO revokeReqDTO) {
        medCheckinService.revokeCheckin(revokeReqDTO);
        return success(true);
    }

    @PostMapping("/verify")
    @Operation(summary = "核实打卡")
    @PreAuthorize("@ss.hasPermission('medication:checkin:verify')")
    @OperateLog(module = "用药打卡系统", name = "核实打卡", type = OperateTypeEnum.UPDATE)
    public CommonResult<Boolean> verifyCheckin(@Valid @RequestBody CheckinVerifyReqDTO verifyReqDTO) {
        medCheckinService.verifyCheckin(verifyReqDTO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取打卡记录详情")
    @Parameter(name = "id", description = "打卡记录ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:checkin:query')")
    public CommonResult<CheckinRespVO> getCheckin(@RequestParam("id") Long id) {
        CheckinRespDTO respDTO = medCheckinService.getCheckinById(id);
        CheckinRespVO respVO = MedCheckinConvert.INSTANCE.toVO(respDTO);
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获取打卡记录分页")
    @PreAuthorize("@ss.hasPermission('medication:checkin:query')")
    public CommonResult<PageResult<CheckinRespVO>> getCheckinPage(@Valid CheckinPageReqVO pageReqVO) {
        CheckinPageReqDTO pageReqDTO = MedCheckinConvert.INSTANCE.toDTO(pageReqVO);
        PageResult<CheckinRespDTO> pageResult = medCheckinService.getCheckinPage(pageReqDTO);
        List<CheckinRespVO> respVOList = MedCheckinConvert.INSTANCE.toVOList(pageResult.getList());
        PageResult<CheckinRespVO> respVOPageResult = new PageResult<>(respVOList, pageResult.getTotal());
        return success(respVOPageResult);
    }

    @GetMapping("/list-by-patient")
    @Operation(summary = "根据患者ID获取打卡记录")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @Parameter(name = "startDate", description = "开始日期")
    @Parameter(name = "endDate", description = "结束日期")
    @PreAuthorize("@ss.hasPermission('medication:checkin:query')")
    public CommonResult<List<CheckinRespVO>> getCheckinsByPatientId(
            @RequestParam("patientId") Long patientId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        List<CheckinRespDTO> respDTOs;
        if (startDate != null && endDate != null) {
            respDTOs = medCheckinService.getCheckinsByPatientIdAndDateRange(patientId, startDate, endDate);
        } else {
            respDTOs = medCheckinService.getCheckinsByPatientIdAndTaskDate(patientId, LocalDate.now());
        }
        
        // 使用Convert转换DTO到VO
        List<CheckinRespVO> respVOs = MedCheckinConvert.INSTANCE.toVOList(respDTOs);
        return success(respVOs);
    }

    @GetMapping("/recent")
    @Operation(summary = "获取最近打卡记录")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @Parameter(name = "days", description = "天数", required = true)
    @PreAuthorize("@ss.hasPermission('medication:checkin:query')")
    public CommonResult<List<CheckinRespVO>> getRecentCheckins(
            @RequestParam("patientId") Long patientId,
            @RequestParam("days") Integer days) {
        
        List<CheckinRespDTO> respDTOs = medCheckinService.getRecentCheckins(patientId, days);
        // 使用Convert转换DTO到VO
        List<CheckinRespVO> respVOs = MedCheckinConvert.INSTANCE.toVOList(respDTOs);
        return success(respVOs);
    }

    @GetMapping("/stats")
    @Operation(summary = "获取打卡统计")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @Parameter(name = "startDate", description = "开始日期", required = true)
    @Parameter(name = "endDate", description = "结束日期", required = true)
    @PreAuthorize("@ss.hasPermission('medication:checkin:query')")
    public CommonResult<CheckinStatsVO> getCheckinStats(
            @RequestParam("patientId") Long patientId,
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        Integer completedCount = medCheckinService.countCompletedCheckins(patientId, startDate, endDate);
        Integer skippedCount = medCheckinService.countSkippedCheckins(patientId, startDate, endDate);
        
        CheckinStatsVO statsVO = new CheckinStatsVO();
        statsVO.setPatientId(patientId);
        statsVO.setStartDate(startDate);
        statsVO.setEndDate(endDate);
        statsVO.setCompletedCount(completedCount);
        statsVO.setSkippedCount(skippedCount);
        statsVO.setTotalCount(completedCount + skippedCount);
        
        return success(statsVO);
    }

    @GetMapping("/check-nonce")
    @Operation(summary = "检查客户端唯一标识")
    @Parameter(name = "clientNonce", description = "客户端唯一标识", required = true)
    public CommonResult<Boolean> checkClientNonce(@RequestParam("clientNonce") String clientNonce) {
        boolean isUsed = medCheckinService.isClientNonceUsed(clientNonce);
        return success(isUsed);
    }

    /**
     * 打卡统计 VO
     */
    public static class CheckinStatsVO {
        private Long patientId;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer completedCount;
        private Integer skippedCount;
        private Integer totalCount;

        // Getters and Setters
        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        
        public Integer getCompletedCount() { return completedCount; }
        public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }
        
        public Integer getSkippedCount() { return skippedCount; }
        public void setSkippedCount(Integer skippedCount) { this.skippedCount = skippedCount; }
        
        public Integer getTotalCount() { return totalCount; }
        public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    }

}
