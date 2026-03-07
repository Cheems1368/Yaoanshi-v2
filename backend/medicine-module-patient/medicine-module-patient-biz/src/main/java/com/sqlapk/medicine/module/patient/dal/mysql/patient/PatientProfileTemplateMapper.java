package com.sqlapk.medicine.module.patient.dal.mysql.patient;

import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.module.patient.dal.dataobject.patient.PatientProfileTemplateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 患者档案模板 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface PatientProfileTemplateMapper extends BaseMapperX<PatientProfileTemplateDO> {

    /**
     * 根据模板类型获取默认模板
     */
    default PatientProfileTemplateDO selectDefaultByType(Integer templateType) {
        return selectOne("template_type", templateType, "is_default", true);
    }

    /**
     * 根据模板类型获取模板列表
     */
    default List<PatientProfileTemplateDO> selectListByType(Integer templateType) {
        return selectList(PatientProfileTemplateDO::getTemplateType, templateType);
    }

    /**
     * 获取所有默认模板
     */
    default List<PatientProfileTemplateDO> selectDefaultTemplates() {
        return selectList(PatientProfileTemplateDO::getIsDefault, true);
    }

    /**
     * 根据模板名称获取模板
     */
    default PatientProfileTemplateDO selectByName(String templateName) {
        return selectOne("template_name", templateName);
    }

}
