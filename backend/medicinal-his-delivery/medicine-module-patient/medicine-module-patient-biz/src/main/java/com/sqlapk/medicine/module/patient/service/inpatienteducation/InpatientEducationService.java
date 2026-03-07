package com.sqlapk.medicine.module.patient.service.inpatienteducation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatienteducation.InpatientEducationDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 住院用药教育 Service 接口
 *
 * @author 管理员
 */
public interface InpatientEducationService {

    /**
     * 创建住院用药教育
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInpatientEducation(@Valid InpatientEducationCreateReqVO createReqVO);

    /**
     * 更新住院用药教育
     *
     * @param updateReqVO 更新信息
     */
    void updateInpatientEducation(@Valid InpatientEducationUpdateReqVO updateReqVO);

    /**
     * 删除住院用药教育
     *
     * @param id 编号
     */
    void deleteInpatientEducation(Long id);

    /**
     * 获得住院用药教育
     *
     * @param id 编号
     * @return 住院用药教育
     */
    InpatientEducationDO getInpatientEducation(Long id);

    /**
     * 获得住院用药教育列表
     *
     * @param ids 编号
     * @return 住院用药教育列表
     */
    List<InpatientEducationDO> getInpatientEducationList(Collection<Long> ids);

    /**
     * 获得住院用药教育分页
     *
     * @param pageReqVO 分页查询
     * @return 住院用药教育分页
     */
    PageResult<InpatientEducationDO> getInpatientEducationPage(InpatientEducationPageReqVO pageReqVO);

    /**
     * 获得住院用药教育列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 住院用药教育列表
     */
    List<InpatientEducationDO> getInpatientEducationList(InpatientEducationExportReqVO exportReqVO);

}
