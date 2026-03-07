package com.sqlapk.medicine.module.patient.service.outpatientmedicine;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicinePageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientmedicine.OutpatientMedicineDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 药学门诊 Service 接口
 *
 * @author 管理员
 */
public interface OutpatientMedicineService {

    /**
     * 创建药学门诊
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOutpatientMedicine(@Valid OutpatientMedicineCreateReqVO createReqVO);

    /**
     * 更新药学门诊
     *
     * @param updateReqVO 更新信息
     */
    void updateOutpatientMedicine(@Valid OutpatientMedicineUpdateReqVO updateReqVO);

    /**
     * 删除药学门诊
     *
     * @param id 编号
     */
    void deleteOutpatientMedicine(Long id);

    /**
     * 获得药学门诊
     *
     * @param id 编号
     * @return 药学门诊
     */
    OutpatientMedicineDO getOutpatientMedicine(Long id);

    /**
     * 获得药学门诊列表
     *
     * @param ids 编号
     * @return 药学门诊列表
     */
    List<OutpatientMedicineDO> getOutpatientMedicineList(Collection<Long> ids);

    /**
     * 获得药学门诊分页
     *
     * @param pageReqVO 分页查询
     * @return 药学门诊分页
     */
    PageResult<OutpatientMedicineDO> getOutpatientMedicinePage(OutpatientMedicinePageReqVO pageReqVO);

    /**
     * 获得药学门诊列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 药学门诊列表
     */
    List<OutpatientMedicineDO> getOutpatientMedicineList(OutpatientMedicineExportReqVO exportReqVO);

}
