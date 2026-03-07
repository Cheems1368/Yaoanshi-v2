package com.sqlapk.medicine.module.medication.convert;

import com.sqlapk.medicine.module.medication.api.dto.ReminderTaskCreateReqDTO;
import com.sqlapk.medicine.module.medication.api.dto.ReminderTaskRespDTO;
import com.sqlapk.medicine.module.medication.dal.dataobject.ReminderTaskDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 提醒任务转换器
 *
 * @author sqlapk
 */
@Mapper
public interface ReminderTaskConvert {

    ReminderTaskConvert INSTANCE = Mappers.getMapper(ReminderTaskConvert.class);

    /**
     * 创建请求转换为DO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "retryCount", ignore = true)
    @Mapping(target = "lastRetryTime", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updater", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    ReminderTaskDO convert(ReminderTaskCreateReqDTO createReqDTO);

    /**
     * DO转换为响应DTO
     */
    @Mapping(target = "typeName", ignore = true)
    @Mapping(target = "statusName", ignore = true)
    ReminderTaskRespDTO convert(ReminderTaskDO source);

    /**
     * 批量转换
     */
    List<ReminderTaskRespDTO> convert(List<ReminderTaskDO> sources);

}
