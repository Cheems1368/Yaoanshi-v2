package com.sqlapk.medicine.module.medication.controller.admin;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.framework.operatelog.core.enums.OperateTypeEnum;
import com.sqlapk.medicine.module.medication.api.MedReportService;
import com.sqlapk.medicine.module.medication.api.dto.ReportGenerateReqDTO;
import com.sqlapk.medicine.module.medication.api.dto.ReportRespDTO;
import com.sqlapk.medicine.module.medication.controller.admin.vo.ReportPageReqVO;
import com.sqlapk.medicine.module.medication.controller.admin.vo.ReportRespVO;
import com.sqlapk.medicine.module.medication.convert.MedReportConvert;
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
 * 管理后台 - 用药报告
 *
 * @author sqlapk
 */
@Tag(name = "管理后台 - 用药报告")
@RestController
@RequestMapping("/medication/report")
@Validated
@Slf4j
public class MedReportController {

    @Autowired
    private MedReportService medReportService;

    @PostMapping("/generate")
    @Operation(summary = "生成报告")
    @PreAuthorize("@ss.hasPermission('medication:report:generate')")
    @OperateLog(module = "用药打卡系统", name = "生成报告", type = OperateTypeEnum.CREATE)
    public CommonResult<Long> generateReport(@Valid @RequestBody ReportGenerateReqDTO generateReqDTO) {
        Long reportId = medReportService.generateReport(generateReqDTO);
        return success(reportId);
    }

    @GetMapping("/get")
    @Operation(summary = "获取报告详情")
    @Parameter(name = "id", description = "报告ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:report:query')")
    public CommonResult<ReportRespVO> getReport(@RequestParam("id") Long id) {
        ReportRespDTO respDTO = medReportService.getReportById(id);
        ReportRespVO respVO = MedReportConvert.INSTANCE.convertDTOToVO(respDTO);
        return success(respVO);
    }

    @GetMapping("/latest")
    @Operation(summary = "获取最新报告")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @Parameter(name = "reportType", description = "报告类型", required = true)
    @PreAuthorize("@ss.hasPermission('medication:report:query')")
    public CommonResult<ReportRespVO> getLatestReport(
            @RequestParam("patientId") Long patientId,
            @RequestParam("reportType") Integer reportType) {
        
        ReportRespDTO respDTO = medReportService.getLatestReport(patientId, reportType);
        ReportRespVO respVO = MedReportConvert.INSTANCE.convertDTOToVO(respDTO);
        return success(respVO);
    }

    @GetMapping("/by-date-range")
    @Operation(summary = "获取指定日期范围报告")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @Parameter(name = "startDate", description = "开始日期", required = true)
    @Parameter(name = "endDate", description = "结束日期", required = true)
    @PreAuthorize("@ss.hasPermission('medication:report:query')")
    public CommonResult<ReportRespVO> getReportByDateRange(
            @RequestParam("patientId") Long patientId,
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        ReportRespDTO respDTO = medReportService.getReportByDateRange(patientId, startDate, endDate);
        ReportRespVO respVO = MedReportConvert.INSTANCE.convertDTOToVO(respDTO);
        return success(respVO);
    }

    @GetMapping("/history")
    @Operation(summary = "获取历史报告")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @Parameter(name = "reportType", description = "报告类型", required = true)
    @PreAuthorize("@ss.hasPermission('medication:report:query')")
    public CommonResult<List<ReportRespVO>> getHistoryReports(
            @RequestParam("patientId") Long patientId,
            @RequestParam("reportType") Integer reportType) {
        
        List<ReportRespDTO> respDTOs = medReportService.getHistoryReports(patientId, reportType);
        List<ReportRespVO> respVOs = MedReportConvert.INSTANCE.toVOList(respDTOs);
        return success(respVOs);
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有报告")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:report:query')")
    public CommonResult<List<ReportRespVO>> getAllReports(@RequestParam("patientId") Long patientId) {
        List<ReportRespDTO> respDTOs = medReportService.getAllReports(patientId);
        List<ReportRespVO> respVOs = MedReportConvert.INSTANCE.toVOList(respDTOs);
        return success(respVOs);
    }

    @GetMapping("/export")
    @Operation(summary = "导出报告")
    @Parameter(name = "id", description = "报告ID", required = true)
    @Parameter(name = "format", description = "导出格式", required = true)
    @PreAuthorize("@ss.hasPermission('medication:report:export')")
    @OperateLog(module = "用药打卡系统", name = "导出报告", type = OperateTypeEnum.EXPORT)
    public CommonResult<String> exportReport(
            @RequestParam("id") Long id,
            @RequestParam("format") String format) {
        
        String filePath = medReportService.exportReport(id, format);
        return success(filePath);
    }

    @PostMapping("/regenerate")
    @Operation(summary = "重新生成报告")
    @Parameter(name = "id", description = "报告ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:report:generate')")
    @OperateLog(module = "用药打卡系统", name = "重新生成报告", type = OperateTypeEnum.UPDATE)
    public CommonResult<Long> regenerateReport(@RequestParam("id") Long id) {
        Long newReportId = medReportService.regenerateReport(id);
        return success(newReportId);
    }

    @PostMapping("/batch-generate")
    @Operation(summary = "批量生成报告")
    @PreAuthorize("@ss.hasPermission('medication:report:generate')")
    @OperateLog(module = "用药打卡系统", name = "批量生成报告", type = OperateTypeEnum.CREATE)
    public CommonResult<Boolean> batchGenerateReports(@RequestBody BatchGenerateReqVO batchReqVO) {
        medReportService.batchGenerateReports(
            batchReqVO.getPatientIds(), 
            batchReqVO.getReportType(), 
            batchReqVO.getStartDate(), 
            batchReqVO.getEndDate()
        );
        return success(true);
    }

    @GetMapping("/status")
    @Operation(summary = "获取报告生成状态")
    @Parameter(name = "id", description = "报告ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:report:query')")
    public CommonResult<Integer> getReportStatus(@RequestParam("id") Long id) {
        Integer status = medReportService.getReportStatus(id);
        return success(status);
    }

    @GetMapping("/7day")
    @Operation(summary = "获取7天报告")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:report:query')")
    public CommonResult<ReportRespVO> get7DayReport(@RequestParam("patientId") Long patientId) {
        ReportRespDTO respDTO = medReportService.get7DayReport(patientId);
        ReportRespVO respVO = MedReportConvert.INSTANCE.convertDTOToVO(respDTO);
        return success(respVO);
    }

    @GetMapping("/30day")
    @Operation(summary = "获取30天报告")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:report:query')")
    public CommonResult<ReportRespVO> get30DayReport(@RequestParam("patientId") Long patientId) {
        ReportRespDTO respDTO = medReportService.get30DayReport(patientId);
        ReportRespVO respVO = MedReportConvert.INSTANCE.convertDTOToVO(respDTO);
        return success(respVO);
    }

    @GetMapping("/90day")
    @Operation(summary = "获取90天报告")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:report:query')")
    public CommonResult<ReportRespVO> get90DayReport(@RequestParam("patientId") Long patientId) {
        ReportRespDTO respDTO = medReportService.get90DayReport(patientId);
        ReportRespVO respVO = MedReportConvert.INSTANCE.convertDTOToVO(respDTO);
        return success(respVO);
    }

    @GetMapping("/quarterly")
    @Operation(summary = "获取季度报告")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @PreAuthorize("@ss.hasPermission('medication:report:query')")
    public CommonResult<ReportRespVO> getQuarterlyReport(@RequestParam("patientId") Long patientId) {
        ReportRespDTO respDTO = medReportService.getQuarterlyReport(patientId);
        ReportRespVO respVO = MedReportConvert.INSTANCE.convertDTOToVO(respDTO);
        return success(respVO);
    }

    @GetMapping("/needs-update")
    @Operation(summary = "检查报告是否需要更新")
    @Parameter(name = "patientId", description = "患者ID", required = true)
    @Parameter(name = "reportType", description = "报告类型", required = true)
    @PreAuthorize("@ss.hasPermission('medication:report:query')")
    public CommonResult<Boolean> needsUpdate(
            @RequestParam("patientId") Long patientId,
            @RequestParam("reportType") Integer reportType) {
        
        boolean needsUpdate = medReportService.needsUpdate(patientId, reportType);
        return success(needsUpdate);
    }

    /**
     * 批量生成请求 VO
     */
    public static class BatchGenerateReqVO {
        private java.util.List<Long> patientIds;
        private Integer reportType;
        private LocalDate startDate;
        private LocalDate endDate;

        // Getters and Setters
        public java.util.List<Long> getPatientIds() { 
            return patientIds != null ? new java.util.ArrayList<>(patientIds) : null; 
        }
        public void setPatientIds(java.util.List<Long> patientIds) { 
            this.patientIds = patientIds != null ? new java.util.ArrayList<>(patientIds) : null; 
        }
        
        public Integer getReportType() { return reportType; }
        public void setReportType(Integer reportType) { this.reportType = reportType; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }

}
