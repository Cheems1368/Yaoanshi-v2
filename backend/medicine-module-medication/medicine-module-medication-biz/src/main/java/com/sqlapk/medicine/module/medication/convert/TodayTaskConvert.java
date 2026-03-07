package com.sqlapk.medicine.module.medication.convert;

import com.sqlapk.medicine.module.medication.api.dto.TodayTaskRespDTO;
import com.sqlapk.medicine.module.medication.dal.dataobject.TodayTaskDO;
import com.sqlapk.medicine.module.medication.enums.TaskStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 今日待办转换器
 *
 * @author sqlapk
 */
@Mapper
public interface TodayTaskConvert {

    TodayTaskConvert INSTANCE = Mappers.getMapper(TodayTaskConvert.class);

    /**
     * DO转换为响应DTO
     */
    @Mapping(target = "statusName", expression = "java(getStatusName(source.getStatus()))")
    @Mapping(target = "drugName", ignore = true)
    @Mapping(target = "dosage", ignore = true)
    @Mapping(target = "instructions", ignore = true)
    @Mapping(target = "mealRelationName", ignore = true)
    TodayTaskRespDTO convert(TodayTaskDO source);

    /**
     * 批量转换
     */
    List<TodayTaskRespDTO> convert(List<TodayTaskDO> sources);

    /**
     * 获取状态名称
     */
    default String getStatusName(Integer status) {
        TaskStatus taskStatus = TaskStatus.valueOf(status);
        return taskStatus != null ? taskStatus.getName() : "未知";
    }

}
