package com.sqlapk.medicine.module.system.convert.helporder;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderCreateReqDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderPageReqDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderRespDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderUpdateReqDTO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderCreateReqVO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderPageReqVO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderRespVO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderUpdateReqVO;
import com.sqlapk.medicine.module.system.dal.dataobject.helporder.HelpOrderDO;
import com.sqlapk.medicine.module.system.enums.HelpOrderStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import java.util.List;

/**
 * 工单 Convert
 *
 * @author sqlapk
 */
@Mapper
public interface HelpOrderConvert {

    HelpOrderConvert INSTANCE = Mappers.getMapper(HelpOrderConvert.class);

    // DTO 转换
    @Mapping(target = "intendedDrugs", source = "intendedDrugs", qualifiedByName = "listToString")
    @Mapping(target = "attachments", source = "attachments", qualifiedByName = "listToString")
    HelpOrderDO convert(HelpOrderCreateReqDTO bean);

    @Mapping(target = "intendedDrugs", source = "intendedDrugs", qualifiedByName = "listToString")
    @Mapping(target = "attachments", source = "attachments", qualifiedByName = "listToString")
    HelpOrderDO convert(HelpOrderUpdateReqDTO bean);

    @Mapping(target = "intendedDrugs", source = "intendedDrugs", qualifiedByName = "stringToList")
    @Mapping(target = "attachments", source = "attachments", qualifiedByName = "stringToList")
    HelpOrderRespDTO convert(HelpOrderDO bean);

    List<HelpOrderRespDTO> convertList(List<HelpOrderDO> list);

    PageResult<HelpOrderRespDTO> convertPage(PageResult<HelpOrderDO> page);

    // VO 转换
    @Mapping(target = "intendedDrugs", source = "intendedDrugs", qualifiedByName = "listToString")
    @Mapping(target = "attachments", source = "attachments", qualifiedByName = "listToString")
    HelpOrderDO convertVO(HelpOrderCreateReqVO bean);

    @Mapping(target = "intendedDrugs", source = "intendedDrugs", qualifiedByName = "listToString")
    @Mapping(target = "attachments", source = "attachments", qualifiedByName = "listToString")
    HelpOrderDO convertVO(HelpOrderUpdateReqVO bean);

    @Mapping(target = "intendedDrugs", source = "intendedDrugs", qualifiedByName = "stringToList")
    @Mapping(target = "attachments", source = "attachments", qualifiedByName = "stringToList")
    HelpOrderRespVO convertVO(HelpOrderDO bean);

    List<HelpOrderRespVO> convertVOList(List<HelpOrderDO> list);

    PageResult<HelpOrderRespVO> convertVOPage(PageResult<HelpOrderDO> page);

    // VO 到 DTO 转换
    @Mapping(target = "status", source = "status", qualifiedByName = "integerToHelpOrderStatus")
    HelpOrderPageReqDTO convert(HelpOrderPageReqVO bean);

    // VO 到 DTO 转换（用于Controller）
    HelpOrderCreateReqDTO convert(HelpOrderCreateReqVO bean);

    HelpOrderUpdateReqDTO convert(HelpOrderUpdateReqVO bean);

    // DTO 到 VO 转换（用于Controller）
    HelpOrderRespVO convert(HelpOrderRespDTO bean);

    // DTO 到 VO 转换（用于Controller）
    List<HelpOrderRespVO> convertRespList(List<HelpOrderRespDTO> list);

    // DTO 到 VO 转换（用于Controller）
    PageResult<HelpOrderRespVO> convertRespPage(PageResult<HelpOrderRespDTO> page);

    // 自定义映射方法
    @Named("stringToList")
    default List<String> stringToList(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Arrays.asList(value.split(","));
    }

    @Named("listToString")
    default String listToString(List<String> value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return String.join(",", value);
    }

    @Named("integerToHelpOrderStatus")
    default HelpOrderStatus integerToHelpOrderStatus(Integer value) {
        if (value == null) {
            return null;
        }
        return HelpOrderStatus.values()[value];
    }

    @Named("helpOrderStatusToInteger")
    default Integer helpOrderStatusToInteger(HelpOrderStatus value) {
        if (value == null) {
            return null;
        }
        return value.ordinal();
    }
}
