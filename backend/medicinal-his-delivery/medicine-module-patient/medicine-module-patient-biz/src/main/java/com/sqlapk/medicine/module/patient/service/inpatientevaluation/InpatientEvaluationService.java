package com.sqlapk.medicine.module.patient.service.inpatientevaluation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientevaluation.InpatientEvaluationDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 住院药学用药评估 Service 接口
 *
 * @author 管理员
 */
public interface InpatientEvaluationService {

    /**
     * 创建住院药学用药评估
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createInpatientEvaluation(@Valid InpatientEvaluationCreateReqVO createReqVO);

    /**
     * 更新住院药学用药评估
     *
     * @param updateReqVO 更新信息
     */
    void updateInpatientEvaluation(@Valid InpatientEvaluationUpdateReqVO updateReqVO);

    /**
     * 删除住院药学用药评估
     *
     * @param id 编号
     */
    void deleteInpatientEvaluation(Long id);

    /**
     * 获得住院药学用药评估
     *
     * @param id 编号
     * @return 住院药学用药评估
     */
    InpatientEvaluationDO getInpatientEvaluation(Long id);

    /**
     * 获得住院药学用药评估列表
     *
     * @param ids 编号
     * @return 住院药学用药评估列表
     */
    List<InpatientEvaluationDO> getInpatientEvaluationList(Collection<Long> ids);

    /**
     * 获得住院药学用药评估分页
     *
     * @param pageReqVO 分页查询
     * @return 住院药学用药评估分页
     */
    PageResult<InpatientEvaluationDO> getInpatientEvaluationPage(InpatientEvaluationPageReqVO pageReqVO);

    /**
     * 获得住院药学用药评估列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 住院药学用药评估列表
     */
    List<InpatientEvaluationDO> getInpatientEvaluationList(InpatientEvaluationExportReqVO exportReqVO);

}
