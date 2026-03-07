package com.sqlapk.medicine.module.medication.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.medication.api.dto.CheckinPageReqDTO;
import com.sqlapk.medicine.module.medication.api.dto.CheckinRespDTO;
import com.sqlapk.medicine.module.medication.controller.admin.vo.CheckinPageReqVO;
import com.sqlapk.medicine.module.medication.controller.admin.vo.CheckinRespVO;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedCheckinDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用药打卡转换器
 *
 * @author sqlapk
 */
@Mapper(componentModel = "spring")
public interface MedCheckinConvert {

    MedCheckinConvert INSTANCE = Mappers.getMapper(MedCheckinConvert.class);

    /**
     * DO转换为响应DTO
     */
    @Mapping(target = "evidence", source = "evidence", qualifiedByName = "stringToEvidenceDTO")
    @Mapping(target = "statusName", ignore = true)
    @Mapping(target = "verifyStatusName", ignore = true)
    CheckinRespDTO toDTO(MedCheckinDO medCheckinDO);

    /**
     * String转换为EvidenceDTO
     */
    @org.mapstruct.Named("stringToEvidenceDTO")
    default CheckinRespDTO.EvidenceDTO stringToEvidenceDTO(String evidenceJson) {
        if (evidenceJson == null || evidenceJson.trim().isEmpty()) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(evidenceJson, CheckinRespDTO.EvidenceDTO.class);
        } catch (JsonProcessingException e) {
            // 如果解析失败，返回null
            return null;
        }
    }

    /**
     * DO列表转换为响应DTO列表
     */
    List<CheckinRespDTO> toDTOList(List<MedCheckinDO> medCheckinDOList);

    /**
     * DO转换为响应VO
     */
    @Mapping(target = "status", expression = "java(medCheckinDO.getStatus() != null ? medCheckinDO.getStatus().toString() : null)")
    CheckinRespVO toVO(MedCheckinDO medCheckinDO);

    /**
     * DO列表转换为响应VO列表
     */
    List<CheckinRespVO> toVOListFromDO(List<MedCheckinDO> medCheckinDOList);

    /**
     * DTO转换为响应VO
     */
    @Mapping(target = "medicationPlanId", source = "todoId")
    @Mapping(target = "remarks", source = "notes")
    CheckinRespVO toVO(CheckinRespDTO checkinRespDTO);

    /**
     * DTO列表转换为响应VO列表
     */
    List<CheckinRespVO> toVOList(List<CheckinRespDTO> checkinRespDTOList);

    /**
     * DO分页结果转换为VO分页结果
     */
    @Mapping(target = "list", source = "list")
    PageResult<CheckinRespVO> toPageResult(PageResult<MedCheckinDO> pageResult);

    /**
     * 分页请求VO转换为DTO
     */
    CheckinPageReqDTO toDTO(CheckinPageReqVO pageReqVO);
}
