package com.sqlapk.medicine.module.medication.dal.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedPlanItemDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用药计划项 Mapper
 *
 * @author sqlapk
 */
@Mapper
public interface MedPlanItemMapper extends BaseMapper<MedPlanItemDO> {

    /**
     * 根据计划ID查询计划项
     *
     * @param planId 计划ID
     * @return 计划项列表
     */
    List<MedPlanItemDO> selectByPlanId(@Param("planId") Long planId);

    /**
     * 根据计划ID删除计划项
     *
     * @param planId 计划ID
     * @return 删除数量
     */
    int deleteByPlanId(@Param("planId") Long planId);

    /**
     * 根据药品名称查询计划项
     *
     * @param drugName 药品名称
     * @return 计划项列表
     */
    List<MedPlanItemDO> selectByDrugName(@Param("drugName") String drugName);

}
