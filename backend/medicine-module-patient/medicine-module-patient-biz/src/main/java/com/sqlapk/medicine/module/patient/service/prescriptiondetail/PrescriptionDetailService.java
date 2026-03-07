package com.sqlapk.medicine.module.patient.service.prescriptiondetail;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptiondetail.PrescriptionDetailDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 患者处方 Service 接口
 *
 * @author 管理员
 */
public interface PrescriptionDetailService {

    /**
     * 创建患者处方
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPrescriptionDetail(@Valid PrescriptionDetailCreateReqVO createReqVO);

    /**
     * 更新患者处方
     *
     * @param updateReqVO 更新信息
     */
    void updatePrescriptionDetail(@Valid PrescriptionDetailUpdateReqVO updateReqVO);

    /**
     * 删除患者处方
     *
     * @param id 编号
     */
    void deletePrescriptionDetail(Long id);

    /**
     * 获得患者处方
     *
     * @param id 编号
     * @return 患者处方
     */
    PrescriptionDetailDO getPrescriptionDetail(Long id);

    /**
     * 获得患者处方列表
     *
     * @param ids 编号
     * @return 患者处方列表
     */
    List<PrescriptionDetailDO> getPrescriptionDetailList(Collection<Long> ids);

    /**
     * 获得患者处方分页
     *
     * @param pageReqVO 分页查询
     * @return 患者处方分页
     */
    PageResult<PrescriptionDetailDO> getPrescriptionDetailPage(PrescriptionDetailPageReqVO pageReqVO);

    /**
     * 获得患者处方列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 患者处方列表
     */
    List<PrescriptionDetailDO> getPrescriptionDetailList(PrescriptionDetailExportReqVO exportReqVO);

}
