package com.sqlapk.medicine.module.patient.service.medicineconsultation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicineconsultation.MedicineConsultationDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 用药咨询 Service 接口
 *
 * @author 管理员
 */
public interface MedicineConsultationService {

    /**
     * 创建用药咨询
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMedicineConsultation(@Valid MedicineConsultationCreateReqVO createReqVO);

    /**
     * 更新用药咨询
     *
     * @param updateReqVO 更新信息
     */
    void updateMedicineConsultation(@Valid MedicineConsultationUpdateReqVO updateReqVO);

    /**
     * 删除用药咨询
     *
     * @param id 编号
     */
    void deleteMedicineConsultation(Long id);

    /**
     * 获得用药咨询
     *
     * @param id 编号
     * @return 用药咨询
     */
    MedicineConsultationDO getMedicineConsultation(Long id);

    /**
     * 获得用药咨询列表
     *
     * @param ids 编号
     * @return 用药咨询列表
     */
    List<MedicineConsultationDO> getMedicineConsultationList(Collection<Long> ids);

    /**
     * 获得用药咨询分页
     *
     * @param pageReqVO 分页查询
     * @return 用药咨询分页
     */
    PageResult<MedicineConsultationDO> getMedicineConsultationPage(MedicineConsultationPageReqVO pageReqVO);

    /**
     * 获得用药咨询列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 用药咨询列表
     */
    List<MedicineConsultationDO> getMedicineConsultationList(MedicineConsultationExportReqVO exportReqVO);

}
