package com.sqlapk.medicine.module.patient.service.medicinereconciliation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicinereconciliation.MedicineReconciliationDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 药物重整剂量 Service 接口
 *
 * @author 管理员
 */
public interface MedicineReconciliationService {

    /**
     * 创建药物重整剂量
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMedicineReconciliation(@Valid MedicineReconciliationCreateReqVO createReqVO);

    /**
     * 更新药物重整剂量
     *
     * @param updateReqVO 更新信息
     */
    void updateMedicineReconciliation(@Valid MedicineReconciliationUpdateReqVO updateReqVO);

    /**
     * 删除药物重整剂量
     *
     * @param id 编号
     */
    void deleteMedicineReconciliation(Long id);

    /**
     * 获得药物重整剂量
     *
     * @param id 编号
     * @return 药物重整剂量
     */
    MedicineReconciliationDO getMedicineReconciliation(Long id);

    /**
     * 获得药物重整剂量列表
     *
     * @param ids 编号
     * @return 药物重整剂量列表
     */
    List<MedicineReconciliationDO> getMedicineReconciliationList(Collection<Long> ids);

    /**
     * 获得药物重整剂量分页
     *
     * @param pageReqVO 分页查询
     * @return 药物重整剂量分页
     */
    PageResult<MedicineReconciliationDO> getMedicineReconciliationPage(MedicineReconciliationPageReqVO pageReqVO);

    /**
     * 获得药物重整剂量列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 药物重整剂量列表
     */
    List<MedicineReconciliationDO> getMedicineReconciliationList(MedicineReconciliationExportReqVO exportReqVO);

}
