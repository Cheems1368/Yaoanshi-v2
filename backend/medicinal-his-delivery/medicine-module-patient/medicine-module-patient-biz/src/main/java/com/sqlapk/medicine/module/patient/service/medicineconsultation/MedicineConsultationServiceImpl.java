package com.sqlapk.medicine.module.patient.service.medicineconsultation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.medicineconsultation.MedicineConsultationConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicineconsultation.MedicineConsultationDO;
import com.sqlapk.medicine.module.patient.dal.mysql.medicineconsultation.MedicineConsultationMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.MEDICINE_CONSULTATION_NOT_EXISTS;

/**
 * 用药咨询 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class MedicineConsultationServiceImpl implements MedicineConsultationService {

    @Resource
    private MedicineConsultationMapper medicineConsultationMapper;

    @Override
    public Long createMedicineConsultation(MedicineConsultationCreateReqVO createReqVO) {
        // 插入
        MedicineConsultationDO medicineConsultation = MedicineConsultationConvert.INSTANCE.convert(createReqVO);
        medicineConsultationMapper.insert(medicineConsultation);
        // 返回
        return medicineConsultation.getId();
    }

    @Override
    public void updateMedicineConsultation(MedicineConsultationUpdateReqVO updateReqVO) {
        // 校验存在
        validateMedicineConsultationExists(updateReqVO.getId());
        // 更新
        MedicineConsultationDO updateObj = MedicineConsultationConvert.INSTANCE.convert(updateReqVO);
        medicineConsultationMapper.updateById(updateObj);
    }

    @Override
    public void deleteMedicineConsultation(Long id) {
        // 校验存在
        validateMedicineConsultationExists(id);
        // 删除
        medicineConsultationMapper.deleteById(id);
    }

    private void validateMedicineConsultationExists(Long id) {
        if (medicineConsultationMapper.selectById(id) == null) {
            throw exception(MEDICINE_CONSULTATION_NOT_EXISTS);
        }
    }

    @Override
    public MedicineConsultationDO getMedicineConsultation(Long id) {
        return medicineConsultationMapper.selectById(id);
    }

    @Override
    public List<MedicineConsultationDO> getMedicineConsultationList(Collection<Long> ids) {
        return medicineConsultationMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<MedicineConsultationDO> getMedicineConsultationPage(MedicineConsultationPageReqVO pageReqVO) {
        return medicineConsultationMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MedicineConsultationDO> getMedicineConsultationList(MedicineConsultationExportReqVO exportReqVO) {
        return medicineConsultationMapper.selectList(exportReqVO);
    }

}
