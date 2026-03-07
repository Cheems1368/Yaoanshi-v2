package com.sqlapk.medicine.module.patient.service.outpatientcheck;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientcheck.OutpatientCheckDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 处方审核 Service 接口
 *
 * @author 管理员
 */
public interface OutpatientCheckService {

    /**
     * 创建处方审核
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOutpatientCheck(@Valid OutpatientCheckCreateReqVO createReqVO);

    /**
     * 更新处方审核
     *
     * @param updateReqVO 更新信息
     */
    void updateOutpatientCheck(@Valid OutpatientCheckUpdateReqVO updateReqVO);

    /**
     * 删除处方审核
     *
     * @param id 编号
     */
    void deleteOutpatientCheck(Long id);

    /**
     * 获得处方审核
     *
     * @param id 编号
     * @return 处方审核
     */
    OutpatientCheckDO getOutpatientCheck(Long id);

    /**
     * 获得处方审核列表
     *
     * @param ids 编号
     * @return 处方审核列表
     */
    List<OutpatientCheckDO> getOutpatientCheckList(Collection<Long> ids);

    /**
     * 获得处方审核分页
     *
     * @param pageReqVO 分页查询
     * @return 处方审核分页
     */
    PageResult<OutpatientCheckDO> getOutpatientCheckPage(OutpatientCheckPageReqVO pageReqVO);

    /**
     * 获得处方审核列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 处方审核列表
     */
    List<OutpatientCheckDO> getOutpatientCheckList(OutpatientCheckExportReqVO exportReqVO);

}
