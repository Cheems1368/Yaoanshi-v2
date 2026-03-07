package com.sqlapk.medicine.module.medication.service.report;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.sqlapk.medicine.module.medication.api.dto.AdherenceStatsRespDTO;
import com.sqlapk.medicine.module.medication.api.dto.CheckinRespDTO;
import com.sqlapk.medicine.module.medication.api.dto.MedPlanRespDTO;
import com.sqlapk.medicine.module.medication.api.dto.ReportRespDTO;
import com.sqlapk.medicine.module.medication.api.MedReportService;
import com.sqlapk.medicine.module.medication.api.MedAdherenceStatsService;
import com.sqlapk.medicine.module.medication.api.MedCheckinService;
import com.sqlapk.medicine.module.medication.api.MedPlanService;
import com.sqlapk.medicine.module.patient.api.PatientsApi;
import com.sqlapk.medicine.module.patient.api.dto.PatientsRespDTO;
import com.sqlapk.medicine.module.medication.util.PdfTableHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PDF报告生成服务
 */
@Slf4j
@Service
public class PDFReportService {

    @Autowired
    private MedReportService medReportService;

    @Autowired
    private MedAdherenceStatsService adherenceStatsService;

    @Autowired
    private MedCheckinService checkinService;

    @Autowired
    private MedPlanService medPlanService;

    @Autowired
    private PatientsApi patientsApi;

    // 字体配置
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLACK);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
    private static final Font SUBHEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);

    /**
     * 生成用药依从性报告PDF
     */
    public byte[] generateAdherenceReportPDF(Long reportId) throws DocumentException, IOException {
        ReportRespDTO report = medReportService.getReportById(reportId);
        if (report == null) {
            throw new IllegalArgumentException("报告不存在");
        }

        PatientsRespDTO patient = patientsApi.getPatients(report.getPatientId());
        if (patient == null) {
            throw new IllegalArgumentException("患者不存在");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);

        document.open();

        try {
            // 添加标题
            addTitle(document, "用药依从性报告");

            // 添加患者信息
            addPatientInfo(document, patient);

            // 添加报告信息
            addReportInfo(document, report);

            // 添加依从性统计
            addAdherenceStats(document, report.getPatientId(), report.getReportType());

            // 添加打卡记录
            addCheckinRecords(document, report.getPatientId(), report.getStartDate());

            // 添加用药计划
            addMedicationPlans(document, report.getPatientId());

            // 添加页脚
            addFooter(document, writer);

        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }

    /**
     * 添加标题
     */
    private void addTitle(Document document, String title) throws DocumentException {
        Paragraph titlePara = new Paragraph(title, TITLE_FONT);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.setSpacingAfter(20);
        document.add(titlePara);
    }

    /**
     * 添加患者信息
     */
    private void addPatientInfo(Document document, PatientsRespDTO patient) throws DocumentException {
        Paragraph header = new Paragraph("患者信息", HEADER_FONT);
        header.setSpacingAfter(10);
        document.add(header);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        PdfTableHelper.addTableRow(table, "患者姓名", patient.getName(), NORMAL_FONT);
        PdfTableHelper.addTableRow(table, "患者性别", patient.getGender() == 0 ? "男" : "女", NORMAL_FONT);
        PdfTableHelper.addTableRow(table, "患者生日", patient.getBirthday() != null ? 
                patient.getBirthday().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "", NORMAL_FONT);
        PdfTableHelper.addTableRow(table, "患者手机号", patient.getMobile(), NORMAL_FONT);
        PdfTableHelper.addTableRow(table, "患者类型", getPatientTypeName(patient.getPatientType()), NORMAL_FONT);
        PdfTableHelper.addTableRow(table, "家庭住址", patient.getAddress(), NORMAL_FONT);

        document.add(table);
    }

    /**
     * 添加报告信息
     */
    private void addReportInfo(Document document, ReportRespDTO report) throws DocumentException {
        Paragraph header = new Paragraph("报告信息", HEADER_FONT);
        header.setSpacingAfter(10);
        document.add(header);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        PdfTableHelper.addTableRow(table, "报告类型", getReportTypeName(report.getReportType()), NORMAL_FONT);
        PdfTableHelper.addTableRow(table, "开始日期", report.getStartDate() != null ? 
                report.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "", NORMAL_FONT);
        PdfTableHelper.addTableRow(table, "生成时间", report.getGenerationTime() != null ? 
                report.getGenerationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "", NORMAL_FONT);
        PdfTableHelper.addTableRow(table, "报告状态", getReportStatusName(report.getStatus()), NORMAL_FONT);

        document.add(table);
    }

    /**
     * 添加依从性统计
     */
    private void addAdherenceStats(Document document, Long patientId, Integer reportType) throws DocumentException {
        Paragraph header = new Paragraph("依从性统计", HEADER_FONT);
        header.setSpacingAfter(10);
        document.add(header);

        // 获取依从性统计数据
        List<AdherenceStatsRespDTO> statsList = adherenceStatsService.getHistoryStats(patientId, reportType);
        
        if (statsList.isEmpty()) {
            Paragraph noData = new Paragraph("暂无依从性统计数据", NORMAL_FONT);
            noData.setSpacingAfter(15);
            document.add(noData);
            return;
        }

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        // 表头
        PdfTableHelper.addTableHeader(table, "统计类型", SUBHEADER_FONT);
        PdfTableHelper.addTableHeader(table, "统计周期", SUBHEADER_FONT);
        PdfTableHelper.addTableHeader(table, "依从率", SUBHEADER_FONT);
        PdfTableHelper.addTableHeader(table, "统计时间", SUBHEADER_FONT);

        // 数据行
        for (AdherenceStatsRespDTO stats : statsList) {
            PdfTableHelper.addTableRow(table, NORMAL_FONT,
                "用药依从性",
                getStatsPeriodName(stats.getPeriod()),
                String.format("%.2f%%", stats.getAdherenceRate()),
                stats.getStatisticsDate() != null ? 
                    stats.getStatisticsDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : ""
            );
        }

        document.add(table);
    }

    /**
     * 添加打卡记录
     */
    private void addCheckinRecords(Document document, Long patientId, LocalDate reportDate) throws DocumentException {
        Paragraph header = new Paragraph("打卡记录", HEADER_FONT);
        header.setSpacingAfter(10);
        document.add(header);

        // 获取打卡记录
        List<CheckinRespDTO> checkinList = checkinService.getCheckinsByPatientId(patientId);
        
        if (checkinList.isEmpty()) {
            Paragraph noData = new Paragraph("暂无打卡记录", NORMAL_FONT);
            noData.setSpacingAfter(15);
            document.add(noData);
            return;
        }

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        // 表头
        PdfTableHelper.addTableHeader(table, "打卡时间", SUBHEADER_FONT);
        PdfTableHelper.addTableHeader(table, "打卡状态", SUBHEADER_FONT);
        PdfTableHelper.addTableHeader(table, "验证状态", SUBHEADER_FONT);
        PdfTableHelper.addTableHeader(table, "备注", SUBHEADER_FONT);
        PdfTableHelper.addTableHeader(table, "创建时间", SUBHEADER_FONT);

        // 数据行
        for (CheckinRespDTO checkin : checkinList) {
            PdfTableHelper.addTableRow(table, NORMAL_FONT,
                checkin.getCheckinTime() != null ? 
                    checkin.getCheckinTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "",
                checkin.getStatusName() != null ? checkin.getStatusName() : getCheckinStatusName(checkin.getStatus()),
                checkin.getVerifyStatusName() != null ? checkin.getVerifyStatusName() : getVerifyStatusName(checkin.getVerifyStatus()),
                checkin.getNotes(),
                checkin.getCreateTime() != null ? 
                    checkin.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : ""
            );
        }

        document.add(table);
    }

    /**
     * 添加用药计划
     */
    private void addMedicationPlans(Document document, Long patientId) throws DocumentException {
        Paragraph header = new Paragraph("用药计划", HEADER_FONT);
        header.setSpacingAfter(10);
        document.add(header);

        // 获取用药计划
        List<MedPlanRespDTO> planList = medPlanService.getMedPlansByPatientId(patientId);
        
        if (planList.isEmpty()) {
            Paragraph noData = new Paragraph("暂无用药计划", NORMAL_FONT);
            noData.setSpacingAfter(15);
            document.add(noData);
            return;
        }

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        // 表头
        PdfTableHelper.addTableHeader(table, "计划名称", SUBHEADER_FONT);
        PdfTableHelper.addTableHeader(table, "计划状态", SUBHEADER_FONT);
        PdfTableHelper.addTableHeader(table, "开始时间", SUBHEADER_FONT);
        PdfTableHelper.addTableHeader(table, "结束时间", SUBHEADER_FONT);

        // 数据行
        for (MedPlanRespDTO plan : planList) {
            PdfTableHelper.addTableRow(table, NORMAL_FONT,
                plan.getPlanName(),
                getPlanStatusName(plan.getStatus()),
                plan.getStartDate() != null ? 
                    plan.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "",
                plan.getEndDate() != null ? 
                    plan.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : ""
            );
        }

        document.add(table);
    }

    /**
     * 添加页脚
     */
    private void addFooter(Document document, PdfWriter writer) throws DocumentException {
        try {
            PdfContentByte cb = writer.getDirectContent();
            cb.beginText();
            cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED), 8);
            cb.setTextMatrix(50, 50);
            cb.showText("用药打卡系统 - 报告生成时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            cb.endText();
        } catch (IOException e) {
            log.error("添加页脚失败", e);
            throw new DocumentException("添加页脚失败", e);
        }
    }


    // 辅助方法
    private String getPatientTypeName(Short patientType) {
        if (patientType == null) return "";
        switch (patientType) {
            case 0: return "住院";
            case 1: return "门诊";
            case 2: return "居家";
            default: return "未知";
        }
    }

    private String getReportTypeName(Integer reportType) {
        if (reportType == null) return "";
        switch (reportType) {
            case 1: return "7天报告";
            case 2: return "30天报告";
            case 3: return "90天报告";
            case 4: return "季度报告";
            default: return "未知";
        }
    }

    private String getReportStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "生成中";
            case 1: return "已完成";
            case 2: return "生成失败";
            default: return "未知";
        }
    }


    private String getStatsPeriodName(Integer statsPeriod) {
        if (statsPeriod == null) return "";
        switch (statsPeriod) {
            case 1: return "7天";
            case 2: return "30天";
            case 3: return "90天";
            default: return "未知";
        }
    }

    private String getCheckinStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待打卡";
            case 1: return "已打卡";
            case 2: return "已跳过";
            case 3: return "已撤销";
            default: return "未知";
        }
    }

    private String getVerifyStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待验证";
            case 1: return "验证通过";
            case 2: return "验证失败";
            default: return "未知";
        }
    }

    private String getPlanStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "草稿";
            case 1: return "活跃";
            case 2: return "暂停";
            case 3: return "完成";
            case 4: return "取消";
            default: return "未知";
        }
    }
}
