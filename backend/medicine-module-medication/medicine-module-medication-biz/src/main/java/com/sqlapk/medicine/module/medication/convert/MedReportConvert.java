package com.sqlapk.medicine.module.medication.convert;

import com.sqlapk.medicine.module.medication.api.dto.ReportRespDTO;
import com.sqlapk.medicine.module.medication.controller.admin.vo.ReportRespVO;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedReportDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用药报告转换器
 *
 * @author sqlapk
 */
@Mapper(componentModel = "spring")
public interface MedReportConvert {

    MedReportConvert INSTANCE = Mappers.getMapper(MedReportConvert.class);

    /**
     * DO转换为响应DTO
     */
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "adherenceSummary", ignore = true)
    @Mapping(target = "medicationSummary", ignore = true)
    @Mapping(target = "healthIndicators", ignore = true)
    @Mapping(target = "reportTypeName", ignore = true)
    @Mapping(target = "statusName", ignore = true)
    ReportRespDTO convert(MedReportDO medReportDO);

    /**
     * DO列表转换为响应DTO列表
     */
    List<ReportRespDTO> convert(List<MedReportDO> medReportDOList);

    /**
     * DO转换为响应VO
     */
    @Mapping(target = "type", expression = "java(medReportDO.getReportType() != null ? medReportDO.getReportType().toString() : null)")
    @Mapping(target = "status", expression = "java(medReportDO.getStatus() != null ? medReportDO.getStatus().toString() : null)")
    @Mapping(target = "generateTime", source = "generationTime")
    ReportRespVO convertToVO(MedReportDO medReportDO);

    /**
     * DO列表转换为响应VO列表
     */
    List<ReportRespVO> convertToVO(List<MedReportDO> medReportDOList);

    /**
     * DTO列表转换为响应VO列表
     */
    List<ReportRespVO> toVOList(List<ReportRespDTO> reportRespDTOList);

    /**
     * DTO转换为DO（用于反向转换）
     */
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "adherenceSummary", ignore = true)
    @Mapping(target = "medicationSummary", ignore = true)
    @Mapping(target = "healthIndicators", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "updater", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    MedReportDO toDO(ReportRespDTO reportRespDTO);

    /**
     * DTO转换为VO（用于Controller层）
     */
    @Mapping(target = "type", expression = "java(reportRespDTO.getReportType() != null ? reportRespDTO.getReportType().toString() : null)")
    @Mapping(target = "status", expression = "java(reportRespDTO.getStatus() != null ? reportRespDTO.getStatus().toString() : null)")
    @Mapping(target = "generateTime", source = "generationTime")
    @Mapping(target = "content", ignore = true)
    ReportRespVO convertDTOToVO(ReportRespDTO reportRespDTO);
}
