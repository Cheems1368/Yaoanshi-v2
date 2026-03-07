package com.sqlapk.medicine.module.patient.service.prescriptionexaminations;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.prescriptionexaminations.PrescriptionExaminationsConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptionexaminations.PrescriptionExaminationsDO;
import com.sqlapk.medicine.module.patient.dal.mysql.prescriptionexaminations.PrescriptionExaminationsMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.PRESCRIPTION_EXAMINATIONS_NOT_EXISTS;

/**
 * 处方检查项目关系 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class PrescriptionExaminationsServiceImpl implements PrescriptionExaminationsService {

    @Resource
    private PrescriptionExaminationsMapper prescriptionExaminationsMapper;

    @Override
    public Long createPrescriptionExaminations(PrescriptionExaminationsCreateReqVO createReqVO) {
        // 插入
        PrescriptionExaminationsDO prescriptionExaminations = PrescriptionExaminationsConvert.INSTANCE.convert(createReqVO);
        prescriptionExaminationsMapper.insert(prescriptionExaminations);
        // 返回
        return prescriptionExaminations.getId();
    }

    @Override
    public void updatePrescriptionExaminations(PrescriptionExaminationsUpdateReqVO updateReqVO) {
        // 校验存在
        validatePrescriptionExaminationsExists(updateReqVO.getId());
        // 更新
        PrescriptionExaminationsDO updateObj = PrescriptionExaminationsConvert.INSTANCE.convert(updateReqVO);
        prescriptionExaminationsMapper.updateById(updateObj);
    }

    @Override
    public void deletePrescriptionExaminations(Long id) {
        // 校验存在
        validatePrescriptionExaminationsExists(id);
        // 删除
        prescriptionExaminationsMapper.deleteById(id);
    }

    private void validatePrescriptionExaminationsExists(Long id) {
        if (prescriptionExaminationsMapper.selectById(id) == null) {
            throw exception(PRESCRIPTION_EXAMINATIONS_NOT_EXISTS);
        }
    }

    @Override
    public PrescriptionExaminationsDO getPrescriptionExaminations(Long id) {
        return prescriptionExaminationsMapper.selectById(id);
    }

    @Override
    public List<PrescriptionExaminationsDO> getPrescriptionExaminationsList(Collection<Long> ids) {
        return prescriptionExaminationsMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<PrescriptionExaminationsDO> getPrescriptionExaminationsPage(PrescriptionExaminationsPageReqVO pageReqVO) {
        return prescriptionExaminationsMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PrescriptionExaminationsDO> getPrescriptionExaminationsList(PrescriptionExaminationsExportReqVO exportReqVO) {
        return prescriptionExaminationsMapper.selectList(exportReqVO);
    }

}
