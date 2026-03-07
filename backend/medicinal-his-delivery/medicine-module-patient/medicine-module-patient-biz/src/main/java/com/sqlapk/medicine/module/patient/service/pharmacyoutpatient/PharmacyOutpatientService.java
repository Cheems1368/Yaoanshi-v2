package com.sqlapk.medicine.module.patient.service.pharmacyoutpatient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.pharmacyoutpatient.PharmacyOutpatientDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 药学门诊 Service 接口
 *
 * @author 管理员
 */
public interface PharmacyOutpatientService {

    /**
     * 创建药学门诊
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPharmacyOutpatient(@Valid PharmacyOutpatientCreateReqVO createReqVO);

    /**
     * 更新药学门诊
     *
     * @param updateReqVO 更新信息
     */
    void updatePharmacyOutpatient(@Valid PharmacyOutpatientUpdateReqVO updateReqVO);

    /**
     * 删除药学门诊
     *
     * @param id 编号
     */
    void deletePharmacyOutpatient(Long id);

    /**
     * 获得药学门诊
     *
     * @param id 编号
     * @return 药学门诊
     */
    PharmacyOutpatientDO getPharmacyOutpatient(Long id);

    /**
     * 获得药学门诊列表
     *
     * @param ids 编号
     * @return 药学门诊列表
     */
    List<PharmacyOutpatientDO> getPharmacyOutpatientList(Collection<Long> ids);

    /**
     * 获得药学门诊分页
     *
     * @param pageReqVO 分页查询
     * @return 药学门诊分页
     */
    PageResult<PharmacyOutpatientDO> getPharmacyOutpatientPage(PharmacyOutpatientPageReqVO pageReqVO);

    /**
     * 获得药学门诊列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 药学门诊列表
     */
    List<PharmacyOutpatientDO> getPharmacyOutpatientList(PharmacyOutpatientExportReqVO exportReqVO);

}
