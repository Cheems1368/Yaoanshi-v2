package com.sqlapk.medicine.module.medication.convert;

import com.sqlapk.medicine.module.medication.api.dto.NotificationRespDTO;
import com.sqlapk.medicine.module.medication.controller.admin.vo.NotificationRespVO;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedNotificationDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用药通知转换器
 *
 * @author sqlapk
 */
@Mapper(componentModel = "spring")
public interface MedNotificationConvert {

    MedNotificationConvert INSTANCE = Mappers.getMapper(MedNotificationConvert.class);

    /**
     * DO转换为响应DTO
     */
    @Mapping(target = "typeName", ignore = true)
    @Mapping(target = "statusName", ignore = true)
    @Mapping(target = "channelName", ignore = true)
    @Mapping(target = "sendStatusName", ignore = true)
    NotificationRespDTO convert(MedNotificationDO medNotificationDO);

    /**
     * DO列表转换为响应DTO列表
     */
    List<NotificationRespDTO> convert(List<MedNotificationDO> medNotificationDOList);

    /**
     * DO转换为响应VO
     */
    @Mapping(target = "type", expression = "java(medNotificationDO.getType() != null ? medNotificationDO.getType().toString() : null)")
    @Mapping(target = "status", expression = "java(medNotificationDO.getStatus() != null ? medNotificationDO.getStatus().toString() : null)")
    @Mapping(target = "medicationPlanId", source = "planId")
    NotificationRespVO convertToVO(MedNotificationDO medNotificationDO);

    /**
     * DO列表转换为响应VO列表
     */
    List<NotificationRespVO> convertToVO(List<MedNotificationDO> medNotificationDOList);

    /**
     * DTO转换为响应VO
     */
    @Mapping(target = "type", expression = "java(notificationRespDTO.getType() != null ? notificationRespDTO.getType().toString() : null)")
    @Mapping(target = "status", expression = "java(notificationRespDTO.getStatus() != null ? notificationRespDTO.getStatus().toString() : null)")
    @Mapping(target = "medicationPlanId", source = "planId")
    NotificationRespVO toVO(NotificationRespDTO notificationRespDTO);

    /**
     * DTO列表转换为响应VO列表
     */
    List<NotificationRespVO> toVOList(List<NotificationRespDTO> notificationRespDTOList);
}
