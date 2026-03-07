package com.sqlapk.medicine.module.patient.service.prescriptions;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptions.PrescriptionsDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 处方 Service 接口
 *
 * @author 管理员
 */
public interface PrescriptionsService {

    /**
     * 创建处方
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPrescriptions(@Valid PrescriptionsCreateReqVO createReqVO);

    /**
     * 更新处方
     *
     * @param updateReqVO 更新信息
     */
    void updatePrescriptions(@Valid PrescriptionsUpdateReqVO updateReqVO);

    /**
     * 删除处方
     *
     * @param id 编号
     */
    void deletePrescriptions(Long id);

    /**
     * 获得处方
     *
     * @param id 编号
     * @return 处方
     */
    PrescriptionsDO getPrescriptions(Long id);

    /**
     * 获得处方列表
     *
     * @param ids 编号
     * @return 处方列表
     */
    List<PrescriptionsDO> getPrescriptionsList(Collection<Long> ids);

    /**
     * 获得处方分页
     *
     * @param pageReqVO 分页查询
     * @return 处方分页
     */
    PageResult<PrescriptionsDO> getPrescriptionsPage(PrescriptionsPageReqVO pageReqVO);

    /**
     * 获得处方列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 处方列表
     */
    List<PrescriptionsDO> getPrescriptionsList(PrescriptionsExportReqVO exportReqVO);

}
