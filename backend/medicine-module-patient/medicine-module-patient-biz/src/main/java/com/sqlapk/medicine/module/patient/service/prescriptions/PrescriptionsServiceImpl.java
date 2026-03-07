package com.sqlapk.medicine.module.patient.service.prescriptions;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.web.core.util.WebFrameworkUtils;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.prescriptiondetail.PrescriptionDetailConvert;
import com.sqlapk.medicine.module.patient.convert.prescriptions.PrescriptionsConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptiondetail.PrescriptionDetailDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptionexaminations.PrescriptionExaminationsDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptions.PrescriptionsDO;
import com.sqlapk.medicine.module.patient.dal.mysql.prescriptiondetail.PrescriptionDetailMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.prescriptionexaminations.PrescriptionExaminationsMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.prescriptions.PrescriptionsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.PRESCRIPTIONS_NOT_EXISTS;

/**
 * 处方 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class PrescriptionsServiceImpl implements PrescriptionsService {

    @Resource
    private PrescriptionsMapper prescriptionsMapper;
    @Resource
    private PrescriptionDetailMapper prescriptionDetailMapper;
    @Resource
    private PrescriptionExaminationsMapper prescriptionExaminationsMapper;
    @Resource
    private PrescriptionDetailConvert prescriptionDetailConvert;
    @Resource
    private PrescriptionsConvert prescriptionsConvert;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPrescriptions(PrescriptionsCreateReqVO createReqVO) {
        // 插入
        PrescriptionsDO prescriptionsDO = PrescriptionsDO.builder()
                .diagnosis(createReqVO.getDiagnosis())
                .doctorId(WebFrameworkUtils.getLoginUserId())
                .type(createReqVO.getType())
                .patientId(createReqVO.getPatientId())
                .build();
        prescriptionsMapper.insert(prescriptionsDO);

        createReqVO.getInspection().forEach(item -> {
            PrescriptionDetailDO detailDO = prescriptionDetailConvert.convert(item);
            detailDO.setId(null);
            detailDO.setPrescriptionId(prescriptionsDO.getId());
            prescriptionDetailMapper.insert(detailDO);
        });

        createReqVO.getExam().forEach(item -> {
            PrescriptionExaminationsDO examinationsDO = new PrescriptionExaminationsDO();
            examinationsDO.setPrescriptionId(prescriptionsDO.getId());
            prescriptionExaminationsMapper.insert(examinationsDO);
        });
        // 返回
        return prescriptionsDO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePrescriptions(PrescriptionsUpdateReqVO updateReqVO) {
        // 校验存在
        validatePrescriptionsExists(updateReqVO.getId());
        // 更新
        PrescriptionsDO updateObj = prescriptionsConvert.convert(updateReqVO);
        prescriptionsMapper.updateById(updateObj);
        // 插入

        LambdaQueryWrapper<PrescriptionDetailDO> detailQuery = new LambdaQueryWrapper<PrescriptionDetailDO>().eq(PrescriptionDetailDO::getPrescriptionId, updateReqVO.getId());
        prescriptionDetailMapper.delete(detailQuery);
        updateReqVO.getInspection().forEach(item -> {
            PrescriptionDetailDO detailDO = prescriptionDetailConvert.convert(item);
            detailDO.setMedicineId(item.getMedicineId());
            detailDO.setId(null);
            detailDO.setPrescriptionId(updateReqVO.getId());
            prescriptionDetailMapper.insert(detailDO);
        });

        LambdaQueryWrapper<PrescriptionExaminationsDO> examQuery = new LambdaQueryWrapper<PrescriptionExaminationsDO>().eq(PrescriptionExaminationsDO::getPrescriptionId, updateReqVO.getId());
        prescriptionExaminationsMapper.delete(examQuery);
        updateReqVO.getExam().forEach(item -> {
            PrescriptionExaminationsDO examinationsDO = new PrescriptionExaminationsDO();
            examinationsDO.setPrescriptionId(updateReqVO.getId());
            examinationsDO.setExaminationId(item.getExaminationId());
            prescriptionExaminationsMapper.insert(examinationsDO);
        });
    }

    @Override
    public void deletePrescriptions(Long id) {
        // 校验存在
        validatePrescriptionsExists(id);
        // 删除
        prescriptionsMapper.deleteById(id);
    }

    private void validatePrescriptionsExists(Long id) {
        if (prescriptionsMapper.selectById(id) == null) {
            throw exception(PRESCRIPTIONS_NOT_EXISTS);
        }
    }

    @Override
    public PrescriptionsDO getPrescriptions(Long id) {
        return prescriptionsMapper.selectById(id);
    }

    @Override
    public List<PrescriptionsDO> getPrescriptionsList(Collection<Long> ids) {
        return prescriptionsMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<PrescriptionsDO> getPrescriptionsPage(PrescriptionsPageReqVO pageReqVO) {
        return prescriptionsMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PrescriptionsDO> getPrescriptionsList(PrescriptionsExportReqVO exportReqVO) {
        return prescriptionsMapper.selectList(exportReqVO);
    }

}
