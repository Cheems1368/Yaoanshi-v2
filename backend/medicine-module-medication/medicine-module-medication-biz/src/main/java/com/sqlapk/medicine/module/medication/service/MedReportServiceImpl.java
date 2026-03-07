package com.sqlapk.medicine.module.medication.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlapk.medicine.framework.common.exception.ServiceException;
import com.sqlapk.medicine.module.medication.api.MedReportService;
import com.sqlapk.medicine.module.medication.api.MedAdherenceStatsService;
import com.sqlapk.medicine.module.medication.api.dto.*;
import com.sqlapk.medicine.module.medication.convert.MedReportConvert;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedReportDO;
import com.sqlapk.medicine.module.medication.dal.mysql.MedReportMapper;
import com.sqlapk.medicine.module.medication.enums.ErrorCodeConstants;
import com.sqlapk.medicine.module.medication.enums.ReportStatus;
import com.sqlapk.medicine.module.medication.enums.ReportType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用药报告服务实现
 *
 * @author sqlapk
 */
@Service
@Slf4j
public class MedReportServiceImpl implements MedReportService {

    @Autowired
    private MedReportMapper medReportMapper;

    @Autowired
    private MedAdherenceStatsService medAdherenceStatsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public Long generateReport(ReportGenerateReqDTO generateReqDTO) {
        log.info("生成报告，患者ID: {}, 报告类型: {}", generateReqDTO.getPatientId(), generateReqDTO.getReportType());

        // 1. 确定日期范围
        LocalDate startDate = generateReqDTO.getStartDate();
        LocalDate endDate = generateReqDTO.getEndDate();
        
        if (startDate == null || endDate == null) {
            ReportType reportType = ReportType.valueOf(generateReqDTO.getReportType());
            if (reportType != null) {
                endDate = LocalDate.now();
                startDate = endDate.minusDays(reportType.getDefaultDays());
            } else {
                throw new ServiceException(ErrorCodeConstants.REPORT_TYPE_INVALID);
            }
        }

        // 2. 检查是否已存在相同报告
        MedReportDO existingReport = medReportMapper.selectByPatientIdAndDateRange(
            generateReqDTO.getPatientId(), startDate, endDate);
        
        if (existingReport != null && ReportStatus.COMPLETED.equals(ReportStatus.valueOf(existingReport.getStatus()))) {
            log.info("报告已存在，返回现有报告ID: {}", existingReport.getId());
            return existingReport.getId();
        }

        // 3. 创建报告记录
        MedReportDO reportDO = MedReportDO.builder()
            .patientId(generateReqDTO.getPatientId())
            .reportType(generateReqDTO.getReportType())
            .title(generateReqDTO.getTitle() != null ? generateReqDTO.getTitle() : 
                generateReportTitle(generateReqDTO.getReportType(), startDate, endDate))
            .startDate(startDate)
            .endDate(endDate)
            .status(ReportStatus.GENERATING.getStatus())
            .version(1)
            .generationTime(LocalDateTime.now())
            .build();

        medReportMapper.insert(reportDO);

        // 4. 异步生成报告内容
        try {
            generateReportContent(reportDO, generateReqDTO);
            
            // 更新状态为已完成
            reportDO.setStatus(ReportStatus.COMPLETED.getStatus());
            medReportMapper.updateById(reportDO);
            
            log.info("报告生成成功，报告ID: {}", reportDO.getId());
            return reportDO.getId();
            
        } catch (Exception e) {
            // 更新状态为生成失败
            reportDO.setStatus(ReportStatus.FAILED.getStatus());
            medReportMapper.updateById(reportDO);
            
            log.error("报告生成失败，报告ID: {}", reportDO.getId(), e);
            throw new ServiceException(ErrorCodeConstants.REPORT_GENERATION_FAILED);
        }
    }

    @Override
    public ReportRespDTO getReportById(Long reportId) {
        log.info("获取报告详情，报告ID: {}", reportId);

        MedReportDO reportDO = medReportMapper.selectById(reportId);
        if (reportDO == null) {
            throw new ServiceException(ErrorCodeConstants.REPORT_NOT_EXISTS);
        }

        ReportRespDTO respDTO = MedReportConvert.INSTANCE.convert(reportDO);
        fillAdditionalFields(respDTO);

        return respDTO;
    }

    @Override
    public ReportRespDTO getLatestReport(Long patientId, Integer reportType) {
        log.info("获取最新报告，患者ID: {}, 报告类型: {}", patientId, reportType);

        MedReportDO reportDO = medReportMapper.selectLatestByPatientIdAndType(patientId, reportType);
        if (reportDO == null) {
            throw new ServiceException(ErrorCodeConstants.REPORT_NOT_EXISTS);
        }

        ReportRespDTO respDTO = MedReportConvert.INSTANCE.convert(reportDO);
        fillAdditionalFields(respDTO);

        return respDTO;
    }

    @Override
    public ReportRespDTO getReportByDateRange(Long patientId, LocalDate startDate, LocalDate endDate) {
        log.info("获取指定日期范围报告，患者ID: {}, 日期范围: {} - {}", patientId, startDate, endDate);

        MedReportDO reportDO = medReportMapper.selectByPatientIdAndDateRange(patientId, startDate, endDate);
        if (reportDO == null) {
            throw new ServiceException(ErrorCodeConstants.REPORT_NOT_EXISTS);
        }

        ReportRespDTO respDTO = MedReportConvert.INSTANCE.convert(reportDO);
        fillAdditionalFields(respDTO);

        return respDTO;
    }

    @Override
    public List<ReportRespDTO> getHistoryReports(Long patientId, Integer reportType) {
        log.info("获取历史报告，患者ID: {}, 报告类型: {}", patientId, reportType);

        List<MedReportDO> reportDOs = medReportMapper.selectHistoryByPatientIdAndType(patientId, reportType);
        List<ReportRespDTO> respDTOs = MedReportConvert.INSTANCE.convert(reportDOs);

        for (ReportRespDTO respDTO : respDTOs) {
            fillAdditionalFields(respDTO);
        }

        return respDTOs;
    }

    @Override
    public List<ReportRespDTO> getAllReports(Long patientId) {
        log.info("获取所有报告，患者ID: {}", patientId);

        List<MedReportDO> reportDOs = medReportMapper.selectByPatientId(patientId);
        List<ReportRespDTO> respDTOs = MedReportConvert.INSTANCE.convert(reportDOs);

        for (ReportRespDTO respDTO : respDTOs) {
            fillAdditionalFields(respDTO);
        }

        return respDTOs;
    }

    @Override
    public String exportReport(Long reportId, String format) {
        log.info("导出报告，报告ID: {}, 格式: {}", reportId, format);

        MedReportDO reportDO = medReportMapper.selectById(reportId);
        if (reportDO == null) {
            throw new ServiceException(ErrorCodeConstants.REPORT_NOT_EXISTS);
        }

        if (!ReportStatus.COMPLETED.equals(ReportStatus.valueOf(reportDO.getStatus()))) {
            throw new ServiceException(ErrorCodeConstants.REPORT_EXPORT_FAILED);
        }

        // 实现具体的导出逻辑
        String fileName = "report_" + reportId + "_" + System.currentTimeMillis() + "." + format.toLowerCase();
        String filePath = "/reports/" + fileName;
        
        try {
            // 根据格式生成文件
            switch (format.toUpperCase()) {
                case "PDF":
                    generatePDFReport(reportDO, filePath);
                    break;
                case "HTML":
                    generateHTMLReport(reportDO, filePath);
                    break;
                case "JSON":
                    generateJSONReport(reportDO, filePath);
                    break;
                default:
                    throw new ServiceException(ErrorCodeConstants.REPORT_EXPORT_FAILED);
            }
            
            // 更新文件信息
            reportDO.setFilePath(filePath);
            reportDO.setFileFormat(format.toUpperCase());
            medReportMapper.updateById(reportDO);
            
            log.info("报告导出成功，报告ID: {}, 文件路径: {}", reportId, filePath);
            return filePath;
            
        } catch (Exception e) {
            log.error("报告导出失败，报告ID: {}", reportId, e);
            throw new ServiceException(ErrorCodeConstants.REPORT_EXPORT_FAILED);
        }
    }

    @Override
    @Transactional
    public Long regenerateReport(Long reportId) {
        log.info("重新生成报告，报告ID: {}", reportId);

        MedReportDO existingReport = medReportMapper.selectById(reportId);
        if (existingReport == null) {
            throw new ServiceException(ErrorCodeConstants.REPORT_NOT_EXISTS);
        }

        // 创建新的生成请求
        ReportGenerateReqDTO generateReqDTO = new ReportGenerateReqDTO();
        generateReqDTO.setPatientId(existingReport.getPatientId());
        generateReqDTO.setReportType(existingReport.getReportType());
        generateReqDTO.setStartDate(existingReport.getStartDate());
        generateReqDTO.setEndDate(existingReport.getEndDate());
        generateReqDTO.setTitle(existingReport.getTitle());

        // 生成新报告
        return generateReport(generateReqDTO);
    }

    @Override
    @Transactional
    public void batchGenerateReports(List<Long> patientIds, Integer reportType, 
                                     LocalDate startDate, LocalDate endDate) {
        log.info("批量生成报告，患者数量: {}, 报告类型: {}", patientIds.size(), reportType);

        for (Long patientId : patientIds) {
            try {
                ReportGenerateReqDTO generateReqDTO = new ReportGenerateReqDTO();
                generateReqDTO.setPatientId(patientId);
                generateReqDTO.setReportType(reportType);
                generateReqDTO.setStartDate(startDate);
                generateReqDTO.setEndDate(endDate);

                generateReport(generateReqDTO);
            } catch (Exception e) {
                log.error("生成患者 {} 的报告失败", patientId, e);
            }
        }

        log.info("批量生成报告完成");
    }

    @Override
    public Integer getReportStatus(Long reportId) {
        MedReportDO reportDO = medReportMapper.selectById(reportId);
        return reportDO != null ? reportDO.getStatus() : null;
    }

    @Override
    public boolean needsUpdate(Long patientId, Integer reportType) {
        MedReportDO latestReport = medReportMapper.selectLatestByPatientIdAndType(patientId, reportType);
        if (latestReport == null) {
            return true;
        }

        // 检查报告日期是否过期（超过1天）
        LocalDate today = LocalDate.now();
        return latestReport.getEndDate().isBefore(today.minusDays(1));
    }

    @Override
    public ReportRespDTO get7DayReport(Long patientId) {
        return getLatestReport(patientId, ReportType.WEEKLY.getType());
    }

    @Override
    public ReportRespDTO get30DayReport(Long patientId) {
        return getLatestReport(patientId, ReportType.MONTHLY.getType());
    }

    @Override
    public ReportRespDTO get90DayReport(Long patientId) {
        return getLatestReport(patientId, ReportType.QUARTERLY.getType());
    }

    @Override
    public ReportRespDTO getQuarterlyReport(Long patientId) {
        return getLatestReport(patientId, ReportType.QUARTERLY.getType());
    }

    /**
     * 生成报告内容
     */
    private void generateReportContent(MedReportDO reportDO, ReportGenerateReqDTO generateReqDTO) {
        // 1. 获取依从统计
        AdherenceStatsRespDTO adherenceStats = medAdherenceStatsService.getStatsByDateRange(
            reportDO.getPatientId(), reportDO.getStartDate(), reportDO.getEndDate());

        // 2. 构建报告内容
        ReportRespDTO.ReportContentDTO content = new ReportRespDTO.ReportContentDTO();
        content.setSummary(generateReportSummary(adherenceStats));
        content.setRecommendations(generateRecommendations(adherenceStats));
        content.setNotes(generateNotes(adherenceStats));

        // 3. 构建依从性摘要
        ReportRespDTO.AdherenceSummaryDTO adherenceSummary = new ReportRespDTO.AdherenceSummaryDTO();
        adherenceSummary.setAdherenceRate(adherenceStats.getAdherenceRate());
        adherenceSummary.setTotalTasks(adherenceStats.getTotalTasks());
        adherenceSummary.setCompletedTasks(adherenceStats.getCompletedTasks());
        adherenceSummary.setMissedTasks(adherenceStats.getMissedTasks());

        // 4. 构建用药摘要
        ReportRespDTO.MedicationSummaryDTO medicationSummary = new ReportRespDTO.MedicationSummaryDTO();
        medicationSummary.setTotalMedications(adherenceStats.getTopMedications() != null ? 
            adherenceStats.getTopMedications().size() : 0);
        medicationSummary.setTopMedication(adherenceStats.getTopMedications() != null && 
            !adherenceStats.getTopMedications().isEmpty() ? 
            adherenceStats.getTopMedications().get(0).getDrugName() : null);
        medicationSummary.setAverageCompletionRate(adherenceStats.getAdherenceRate());

        // 5. 构建健康指标（占位）
        ReportRespDTO.HealthIndicatorsDTO healthIndicators = new ReportRespDTO.HealthIndicatorsDTO();
        healthIndicators.setBloodPressure("120/80");
        healthIndicators.setHeartRate("72");
        healthIndicators.setWeight("70kg");
        healthIndicators.setBloodSugar("6.5");
        healthIndicators.setBmi("22.5");

        // 6. 保存到数据库
        try {
            reportDO.setContent(convertToJson(content));
            reportDO.setAdherenceSummary(convertToJson(adherenceSummary));
            reportDO.setMedicationSummary(convertToJson(medicationSummary));
            reportDO.setHealthIndicators(convertToJson(healthIndicators));
        } catch (Exception e) {
            log.error("转换报告内容为JSON失败", e);
            throw new ServiceException(ErrorCodeConstants.REPORT_GENERATION_FAILED);
        }
    }

    /**
     * 生成报告摘要
     */
    private String generateReportSummary(AdherenceStatsRespDTO adherenceStats) {
        return String.format("患者用药依从性%s，在统计期间共完成%d次用药，依从率为%s。",
            adherenceStats.getAdherenceRate().compareTo(java.math.BigDecimal.valueOf(90)) >= 0 ? "良好" : "需要改善",
            adherenceStats.getCompletedTasks(),
            adherenceStats.getAdherenceRatePercent());
    }

    /**
     * 生成建议
     */
    private String generateRecommendations(AdherenceStatsRespDTO adherenceStats) {
        if (adherenceStats.getAdherenceRate().compareTo(java.math.BigDecimal.valueOf(90)) >= 0) {
            return "继续保持良好的用药习惯，按时服药。";
        } else {
            return "建议加强用药管理，设置用药提醒，提高依从性。";
        }
    }

    /**
     * 生成注意事项
     */
    private String generateNotes(AdherenceStatsRespDTO adherenceStats) {
        return "请定期监测相关健康指标，如有异常请及时就医。";
    }

    /**
     * 生成报告标题
     */
    private String generateReportTitle(Integer reportType, LocalDate startDate, LocalDate endDate) {
        ReportType type = ReportType.valueOf(reportType);
        if (type != null) {
            return String.format("%s用药报告（%s - %s）", 
                type.getName(), startDate, endDate);
        }
        return String.format("用药报告（%s - %s）", startDate, endDate);
    }

    /**
     * 转换为JSON
     */
    private String convertToJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * 填充额外字段
     */
    private void fillAdditionalFields(ReportRespDTO respDTO) {
        // 填充报告类型名称
        ReportType reportType = ReportType.valueOf(respDTO.getReportType());
        respDTO.setReportTypeName(reportType != null ? reportType.getName() : "未知");

        // 填充状态名称
        ReportStatus status = ReportStatus.valueOf(respDTO.getStatus());
        respDTO.setStatusName(status != null ? status.getName() : "未知");
    }

    /**
     * 生成PDF报告
     */
    private void generatePDFReport(MedReportDO reportDO, String filePath) throws Exception {
        log.info("生成PDF报告，报告ID: {}, 文件路径: {}", reportDO.getId(), filePath);
        
        try {
            // 使用iText生成PDF
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(filePath));
            document.open();
            
            // 添加标题
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(reportDO.getTitle(), titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(title);
            
            // 添加基本信息
            com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12);
            document.add(new com.itextpdf.text.Paragraph("报告ID: " + reportDO.getId(), normalFont));
            document.add(new com.itextpdf.text.Paragraph("患者ID: " + reportDO.getPatientId(), normalFont));
            document.add(new com.itextpdf.text.Paragraph("报告类型: " + getReportTypeName(reportDO.getReportType()), normalFont));
            document.add(new com.itextpdf.text.Paragraph("统计周期: " + reportDO.getStartDate() + " 至 " + reportDO.getEndDate(), normalFont));
            document.add(new com.itextpdf.text.Paragraph("生成时间: " + LocalDateTime.now(), normalFont));
            
            // 添加报告内容
            if (reportDO.getContent() != null && !reportDO.getContent().isEmpty()) {
                document.add(new com.itextpdf.text.Paragraph("\n报告内容:", normalFont));
                document.add(new com.itextpdf.text.Paragraph(reportDO.getContent(), normalFont));
            }
            
            // 添加页脚
            document.add(new com.itextpdf.text.Paragraph("\n\n--- 用药打卡系统自动生成 ---", normalFont));
            
            document.close();
            
        } catch (Exception e) {
            log.error("PDF生成失败，使用备用方案", e);
            // 备用方案：生成简单的文本文件
            java.nio.file.Files.write(
                java.nio.file.Paths.get(filePath),
                generateTextReport(reportDO).getBytes(StandardCharsets.UTF_8)
            );
        }
    }

    /**
     * 生成HTML报告
     */
    private void generateHTMLReport(MedReportDO reportDO, String filePath) throws Exception {
        log.info("生成HTML报告，报告ID: {}, 文件路径: {}", reportDO.getId(), filePath);
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"zh-CN\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>").append(reportDO.getTitle()).append("</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: 'Microsoft YaHei', Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }\n");
        html.append("        .container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n");
        html.append("        .header { text-align: center; border-bottom: 2px solid #007bff; padding-bottom: 20px; margin-bottom: 30px; }\n");
        html.append("        .title { color: #007bff; font-size: 28px; font-weight: bold; margin: 0; }\n");
        html.append("        .subtitle { color: #666; font-size: 14px; margin-top: 10px; }\n");
        html.append("        .info-section { margin-bottom: 25px; }\n");
        html.append("        .info-title { color: #333; font-size: 18px; font-weight: bold; margin-bottom: 15px; border-left: 4px solid #007bff; padding-left: 10px; }\n");
        html.append("        .info-item { margin-bottom: 8px; color: #555; }\n");
        html.append("        .info-label { font-weight: bold; color: #333; }\n");
        html.append("        .content-section { margin-top: 30px; }\n");
        html.append("        .content-text { line-height: 1.6; color: #444; white-space: pre-wrap; }\n");
        html.append("        .footer { text-align: center; margin-top: 40px; padding-top: 20px; border-top: 1px solid #eee; color: #999; font-size: 12px; }\n");
        html.append("        .highlight { background-color: #fff3cd; padding: 15px; border-radius: 5px; border-left: 4px solid #ffc107; margin: 15px 0; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <div class=\"header\">\n");
        html.append("            <h1 class=\"title\">").append(reportDO.getTitle()).append("</h1>\n");
        html.append("            <div class=\"subtitle\">用药打卡系统自动生成</div>\n");
        html.append("        </div>\n");
        
        html.append("        <div class=\"info-section\">\n");
        html.append("            <div class=\"info-title\">基本信息</div>\n");
        html.append("            <div class=\"info-item\"><span class=\"info-label\">报告ID：</span>").append(reportDO.getId()).append("</div>\n");
        html.append("            <div class=\"info-item\"><span class=\"info-label\">患者ID：</span>").append(reportDO.getPatientId()).append("</div>\n");
        html.append("            <div class=\"info-item\"><span class=\"info-label\">报告类型：</span>").append(getReportTypeName(reportDO.getReportType())).append("</div>\n");
        html.append("            <div class=\"info-item\"><span class=\"info-label\">统计周期：</span>").append(reportDO.getStartDate()).append(" 至 ").append(reportDO.getEndDate()).append("</div>\n");
        html.append("            <div class=\"info-item\"><span class=\"info-label\">生成时间：</span>").append(LocalDateTime.now()).append("</div>\n");
        html.append("        </div>\n");
        
        if (reportDO.getContent() != null && !reportDO.getContent().isEmpty()) {
            html.append("        <div class=\"content-section\">\n");
            html.append("            <div class=\"info-title\">报告内容</div>\n");
            html.append("            <div class=\"highlight\">\n");
            html.append("                <div class=\"content-text\">").append(reportDO.getContent()).append("</div>\n");
            html.append("            </div>\n");
            html.append("        </div>\n");
        }
        
        html.append("        <div class=\"footer\">\n");
        html.append("            <p>本报告由用药打卡系统自动生成，仅供参考</p>\n");
        html.append("            <p>如有疑问，请联系您的医生或药师</p>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        java.nio.file.Files.write(
            java.nio.file.Paths.get(filePath),
            html.toString().getBytes("UTF-8")
        );
    }

    /**
     * 生成JSON报告
     */
    private void generateJSONReport(MedReportDO reportDO, String filePath) throws Exception {
        log.info("生成JSON报告，报告ID: {}, 文件路径: {}", reportDO.getId(), filePath);
        
        ReportRespDTO reportDTO = MedReportConvert.INSTANCE.convert(reportDO);
        fillAdditionalFields(reportDTO);
        
        // 美化JSON格式
        String jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(reportDTO);
        
        java.nio.file.Files.write(
            java.nio.file.Paths.get(filePath),
            jsonContent.getBytes("UTF-8")
        );
    }

    /**
     * 生成文本报告（备用方案）
     */
    private String generateTextReport(MedReportDO reportDO) {
        StringBuilder text = new StringBuilder();
        text.append("=".repeat(50)).append("\n");
        text.append("            ").append(reportDO.getTitle()).append("\n");
        text.append("=".repeat(50)).append("\n\n");
        
        text.append("基本信息：\n");
        text.append("  报告ID：").append(reportDO.getId()).append("\n");
        text.append("  患者ID：").append(reportDO.getPatientId()).append("\n");
        text.append("  报告类型：").append(getReportTypeName(reportDO.getReportType())).append("\n");
        text.append("  统计周期：").append(reportDO.getStartDate()).append(" 至 ").append(reportDO.getEndDate()).append("\n");
        text.append("  生成时间：").append(LocalDateTime.now()).append("\n\n");
        
        if (reportDO.getContent() != null && !reportDO.getContent().isEmpty()) {
            text.append("报告内容：\n");
            text.append("-".repeat(30)).append("\n");
            text.append(reportDO.getContent()).append("\n");
            text.append("-".repeat(30)).append("\n\n");
        }
        
        text.append("本报告由用药打卡系统自动生成，仅供参考\n");
        text.append("如有疑问，请联系您的医生或药师\n");
        text.append("=".repeat(50));
        
        return text.toString();
    }

    /**
     * 获取报告类型名称
     */
    private String getReportTypeName(Integer reportType) {
        ReportType type = ReportType.valueOf(reportType);
        return type != null ? type.getName() : "未知";
    }

}
