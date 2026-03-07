package com.sqlapk.medicine.module.medication.convert;

import com.sqlapk.medicine.module.medication.api.dto.*;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedPlanDO;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedPlanItemDO;
import com.sqlapk.medicine.module.medication.enums.MealRelation;
import com.sqlapk.medicine.module.medication.enums.MedPlanStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用药计划转换器
 *
 * @author sqlapk
 */
@Mapper(componentModel = "spring")
public interface MedPlanConvert {

    MedPlanConvert INSTANCE = Mappers.getMapper(MedPlanConvert.class);

    /**
     * 创建请求转换为DO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updater", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    MedPlanDO convert(MedPlanCreateReqDTO createReqDTO);

    /**
     * 更新请求转换为DO
     */
    @Mapping(target = "patientId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updater", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    MedPlanDO convert(MedPlanUpdateReqDTO updateReqDTO);

    /**
     * DO转换为响应DTO
     */
    @Mapping(target = "statusName", expression = "java(getStatusName(source.getStatus()))")
    @Mapping(target = "items", ignore = true)
    MedPlanRespDTO convert(MedPlanDO source);

    /**
     * 创建请求项转换为DO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "planId", ignore = true)
    @Mapping(target = "timePoints", expression = "java(convertTimePointsToString(source.getTimePoints()))")
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updater", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    MedPlanItemDO convert(MedPlanItemCreateReqDTO source);

    /**
     * 更新请求项转换为DO
     */
    @Mapping(target = "planId", ignore = true)
    @Mapping(target = "timePoints", expression = "java(convertTimePointsToString(source.getTimePoints()))")
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updater", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    MedPlanItemDO convert(MedPlanItemUpdateReqDTO source);

    /**
     * DO转换为响应DTO
     */
    @Mapping(target = "timePoints", expression = "java(convertTimePointsFromString(source.getTimePoints()))")
    @Mapping(target = "mealRelationName", expression = "java(getMealRelationName(source.getMealRelation()))")
    MedPlanItemRespDTO convert(MedPlanItemDO source);

    /**
     * 批量转换
     */
    List<MedPlanItemDO> convert(List<MedPlanItemCreateReqDTO> sources);
    List<MedPlanItemDO> convertUpdate(List<MedPlanItemUpdateReqDTO> sources);
    List<MedPlanItemRespDTO> convertItems(List<MedPlanItemDO> sources);
    List<MedPlanRespDTO> convertToRespDTO(List<MedPlanDO> sources);
    List<MedPlanRespDTO> convertList(List<MedPlanDO> sources);

    /**
     * 获取状态名称
     */
    default String getStatusName(Integer status) {
        MedPlanStatus medPlanStatus = MedPlanStatus.valueOf(status);
        return medPlanStatus != null ? medPlanStatus.getName() : "未知";
    }

    /**
     * 获取餐前后关系名称
     */
    default String getMealRelationName(Integer mealRelation) {
        MealRelation relation = MealRelation.valueOf(mealRelation);
        return relation != null ? relation.getName() : "未知";
    }

    /**
     * 时间点列表转换为JSON字符串
     */
    default String convertTimePointsToString(List<String> timePoints) {
        if (timePoints == null || timePoints.isEmpty()) {
            return "[]";
        }
        return "[\"" + String.join("\",\"", timePoints) + "\"]";
    }

    /**
     * JSON字符串转换为时间点列表
     */
    default List<String> convertTimePointsFromString(String timePointsJson) {
        if (timePointsJson == null || timePointsJson.trim().isEmpty()) {
            return List.of();
        }
        // 简单的JSON解析，实际项目中可以使用Jackson
        String cleaned = timePointsJson.replaceAll("[\\[\\]\"]", "");
        if (cleaned.isEmpty()) {
            return List.of();
        }
        return List.of(cleaned.split(","));
    }

}
