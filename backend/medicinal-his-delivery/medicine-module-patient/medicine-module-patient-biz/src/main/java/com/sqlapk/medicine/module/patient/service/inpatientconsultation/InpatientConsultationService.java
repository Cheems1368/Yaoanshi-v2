package com.sqlapk.medicine.module.patient.service.inpatientconsultation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientconsultation.InpatientConsultationDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 主要用药咨询 Service 接口
 *
 * @author 管理员
 */
public interface InpatientConsultationService {

    /**
     * 创建主要用药咨询
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInpatientConsultation(@Valid InpatientConsultationCreateReqVO createReqVO);

    /**
     * 更新主要用药咨询
     *
     * @param updateReqVO 更新信息
     */
    void updateInpatientConsultation(@Valid InpatientConsultationUpdateReqVO updateReqVO);

    /**
     * 删除主要用药咨询
     *
     * @param id 编号
     */
    void deleteInpatientConsultation(Long id);

    /**
     * 获得主要用药咨询
     *
     * @param id 编号
     * @return 主要用药咨询
     */
    InpatientConsultationDO getInpatientConsultation(Long id);

    /**
     * 获得主要用药咨询列表
     *
     * @param ids 编号
     * @return 主要用药咨询列表
     */
    List<InpatientConsultationDO> getInpatientConsultationList(Collection<Long> ids);

    /**
     * 获得主要用药咨询分页
     *
     * @param pageReqVO 分页查询
     * @return 主要用药咨询分页
     */
    PageResult<InpatientConsultationDO> getInpatientConsultationPage(InpatientConsultationPageReqVO pageReqVO);

    /**
     * 获得主要用药咨询列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 主要用药咨询列表
     */
    List<InpatientConsultationDO> getInpatientConsultationList(InpatientConsultationExportReqVO exportReqVO);

}
