package com.sqlapk.medicine.module.patient.service.education;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.education.EducationDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 用药教育 Service 接口
 *
 * @author 管理员
 */
public interface EducationService {

    /**
     * 创建用药教育
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createEduction(@Valid EducationCreateReqVO createReqVO);

    /**
     * 更新用药教育
     *
     * @param updateReqVO 更新信息
     */
    void updateEduction(@Valid EducationUpdateReqVO updateReqVO);

    /**
     * 删除用药教育
     *
     * @param id 编号
     */
    void deleteEduction(Long id);

    /**
     * 获得用药教育
     *
     * @param id 编号
     * @return 用药教育
     */
    EducationDO getEduction(Long id);

    /**
     * 获得用药教育列表
     *
     * @param ids 编号
     * @return 用药教育列表
     */
    List<EducationDO> getEductionList(Collection<Long> ids);

    /**
     * 获得用药教育分页
     *
     * @param pageReqVO 分页查询
     * @return 用药教育分页
     */
    PageResult<EducationDO> getEductionPage(EducationPageReqVO pageReqVO);

    /**
     * 获得用药教育列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 用药教育列表
     */
    List<EducationDO> getEductionList(EducationExportReqVO exportReqVO);

}
