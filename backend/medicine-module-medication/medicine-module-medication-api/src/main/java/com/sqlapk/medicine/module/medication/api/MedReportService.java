package com.sqlapk.medicine.module.medication.api;

import com.sqlapk.medicine.module.medication.api.dto.ReportGenerateReqDTO;
import com.sqlapk.medicine.module.medication.api.dto.ReportRespDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 用药报告服务 API
 *
 * @author sqlapk
 */
public interface MedReportService {

    /**
     * 生成报告
     *
     * @param generateReqDTO 生成请求
     * @return 报告ID
     */
    Long generateReport(ReportGenerateReqDTO generateReqDTO);

    /**
     * 获取报告详情
     *
     * @param reportId 报告ID
     * @return 报告详情
     */
    ReportRespDTO getReportById(Long reportId);

    /**
     * 获取患者最新报告
     *
     * @param patientId 患者ID
     * @param reportType 报告类型
     * @return 报告详情
     */
    ReportRespDTO getLatestReport(Long patientId, Integer reportType);

    /**
     * 获取患者指定日期范围的报告
     *
     * @param patientId 患者ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报告详情
     */
    ReportRespDTO getReportByDateRange(Long patientId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取患者历史报告
     *
     * @param patientId 患者ID
     * @param reportType 报告类型
     * @return 历史报告列表
     */
    List<ReportRespDTO> getHistoryReports(Long patientId, Integer reportType);

    /**
     * 获取患者所有报告
     *
     * @param patientId 患者ID
     * @return 报告列表
     */
    List<ReportRespDTO> getAllReports(Long patientId);

    /**
     * 导出报告
     *
     * @param reportId 报告ID
     * @param format 导出格式(PDF,HTML,JSON)
     * @return 文件路径
     */
    String exportReport(Long reportId, String format);

    /**
     * 重新生成报告
     *
     * @param reportId 报告ID
     * @return 新报告ID
     */
    Long regenerateReport(Long reportId);

    /**
     * 批量生成报告
     *
     * @param patientIds 患者ID列表
     * @param reportType 报告类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    void batchGenerateReports(List<Long> patientIds, Integer reportType, 
                             LocalDate startDate, LocalDate endDate);

    /**
     * 获取报告生成状态
     *
     * @param reportId 报告ID
     * @return 生成状态
     */
    Integer getReportStatus(Long reportId);

    /**
     * 检查报告是否需要更新
     *
     * @param patientId 患者ID
     * @param reportType 报告类型
     * @return 是否需要更新
     */
    boolean needsUpdate(Long patientId, Integer reportType);

    /**
     * 获取患者7天报告
     *
     * @param patientId 患者ID
     * @return 7天报告
     */
    ReportRespDTO get7DayReport(Long patientId);

    /**
     * 获取患者30天报告
     *
     * @param patientId 患者ID
     * @return 30天报告
     */
    ReportRespDTO get30DayReport(Long patientId);

    /**
     * 获取患者90天报告
     *
     * @param patientId 患者ID
     * @return 90天报告
     */
    ReportRespDTO get90DayReport(Long patientId);

    /**
     * 获取患者季度报告
     *
     * @param patientId 患者ID
     * @return 季度报告
     */
    ReportRespDTO getQuarterlyReport(Long patientId);

}
