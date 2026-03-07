package com.sqlapk.medicine.module.patient.service.medicinereconciliationdetail;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicinereconciliationdetail.MedicineReconciliationDetailDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 药物重整剂量 Service 接口
 *
 * @author 管理员
 */
public interface MedicineReconciliationDetailService {

    /**
     * 创建药物重整剂量
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMedicineReconciliationDetail(@Valid MedicineReconciliationDetailCreateReqVO createReqVO);

    /**
     * 更新药物重整剂量
     *
     * @param updateReqVO 更新信息
     */
    void updateMedicineReconciliationDetail(@Valid MedicineReconciliationDetailUpdateReqVO updateReqVO);

    /**
     * 删除药物重整剂量
     *
     * @param id 编号
     */
    void deleteMedicineReconciliationDetail(Long id);

    /**
     * 获得药物重整剂量
     *
     * @param id 编号
     * @return 药物重整剂量
     */
    MedicineReconciliationDetailDO getMedicineReconciliationDetail(Long id);

    /**
     * 获得药物重整剂量列表
     *
     * @param ids 编号
     * @return 药物重整剂量列表
     */
    List<MedicineReconciliationDetailDO> getMedicineReconciliationDetailList(Collection<Long> ids);

    /**
     * 获得药物重整剂量分页
     *
     * @param pageReqVO 分页查询
     * @return 药物重整剂量分页
     */
    PageResult<MedicineReconciliationDetailDO> getMedicineReconciliationDetailPage(MedicineReconciliationDetailPageReqVO pageReqVO);

    /**
     * 获得药物重整剂量列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 药物重整剂量列表
     */
    List<MedicineReconciliationDetailDO> getMedicineReconciliationDetailList(MedicineReconciliationDetailExportReqVO exportReqVO);

}
