package com.sqlapk.medicine.module.patient.service.outpatienteducation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatienteducation.OutpatientEducationDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 门诊处方用药教育明细 Service 接口
 *
 * @author 管理员
 */
public interface OutpatientEducationService {

    /**
     * 创建门诊处方用药教育明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOutpatientEducation(@Valid OutpatientEducationCreateReqVO createReqVO);

    /**
     * 更新门诊处方用药教育明细
     *
     * @param updateReqVO 更新信息
     */
    void updateOutpatientEducation(@Valid OutpatientEducationUpdateReqVO updateReqVO);

    /**
     * 删除门诊处方用药教育明细
     *
     * @param id 编号
     */
    void deleteOutpatientEducation(Long id);

    /**
     * 获得门诊处方用药教育明细
     *
     * @param id 编号
     * @return 门诊处方用药教育明细
     */
    OutpatientEducationDO getOutpatientEducation(Long id);

    /**
     * 获得门诊处方用药教育明细列表
     *
     * @param ids 编号
     * @return 门诊处方用药教育明细列表
     */
    List<OutpatientEducationDO> getOutpatientEducationList(Collection<Long> ids);

    /**
     * 获得门诊处方用药教育明细分页
     *
     * @param pageReqVO 分页查询
     * @return 门诊处方用药教育明细分页
     */
    PageResult<OutpatientEducationDO> getOutpatientEducationPage(OutpatientEducationPageReqVO pageReqVO);

    /**
     * 获得门诊处方用药教育明细列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 门诊处方用药教育明细列表
     */
    List<OutpatientEducationDO> getOutpatientEducationList(OutpatientEducationExportReqVO exportReqVO);

}
