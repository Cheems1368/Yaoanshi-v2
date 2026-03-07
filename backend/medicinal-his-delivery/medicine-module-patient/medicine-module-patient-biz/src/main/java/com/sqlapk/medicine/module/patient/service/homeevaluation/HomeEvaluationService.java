package com.sqlapk.medicine.module.patient.service.homeevaluation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeevaluation.HomeEvaluationDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 居家随访评估 Service 接口
 *
 * @author 管理员
 */
public interface HomeEvaluationService {

    /**
     * 创建居家随访评估
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createHomeEvaluation(@Valid HomeEvaluationCreateReqVO createReqVO);

    /**
     * 更新居家随访评估
     *
     * @param updateReqVO 更新信息
     */
    void updateHomeEvaluation(@Valid HomeEvaluationUpdateReqVO updateReqVO);

    /**
     * 删除居家随访评估
     *
     * @param id 编号
     */
    void deleteHomeEvaluation(Long id);

    /**
     * 获得居家随访评估
     *
     * @param id 编号
     * @return 居家随访评估
     */
    HomeEvaluationDO getHomeEvaluation(Long id);

    /**
     * 获得居家随访评估列表
     *
     * @param ids 编号
     * @return 居家随访评估列表
     */
    List<HomeEvaluationDO> getHomeEvaluationList(Collection<Long> ids);

    /**
     * 获得居家随访评估分页
     *
     * @param pageReqVO 分页查询
     * @return 居家随访评估分页
     */
    PageResult<HomeEvaluationDO> getHomeEvaluationPage(HomeEvaluationPageReqVO pageReqVO);

    /**
     * 获得居家随访评估列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 居家随访评估列表
     */
    List<HomeEvaluationDO> getHomeEvaluationList(HomeEvaluationExportReqVO exportReqVO);

}
