package com.sqlapk.medicine.module.patient.service.outpatient;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.drugs.dal.dataobject.drug.DrugsDO;
import com.sqlapk.medicine.module.drugs.service.drug.DrugsService;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientPatientInfoRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientUpdateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsExportReqVO;
import com.sqlapk.medicine.module.patient.convert.outpatient.OutpatientConvert;
import com.sqlapk.medicine.module.patient.convert.outpatientcheck.OutpatientCheckConvert;
import com.sqlapk.medicine.module.patient.convert.prescriptiondetail.PrescriptionDetailConvert;
import com.sqlapk.medicine.module.patient.convert.prescriptionexaminations.PrescriptionExaminationsConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.education.EducationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.examinations.ExaminationsDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatient.OutpatientDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientcheck.OutpatientCheckDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatienteducation.OutpatientEducationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientexam.OutpatientExamDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientmedicine.OutpatientMedicineDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.pharmacyoutpatient.PharmacyOutpatientDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptiondetail.PrescriptionDetailDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptionexaminations.PrescriptionExaminationsDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptions.PrescriptionsDO;
import com.sqlapk.medicine.module.patient.dal.mysql.education.EducationMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.outpatient.OutpatientMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.outpatientcheck.OutpatientCheckMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.outpatienteducation.OutpatientEducationMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.outpatientexam.OutpatientExamMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.outpatientmedicine.OutpatientMedicineMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.pharmacyoutpatient.PharmacyOutpatientMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.prescriptiondetail.PrescriptionDetailMapper;
import com.sqlapk.medicine.module.patient.dal.mysql.prescriptions.PrescriptionsMapper;
import com.sqlapk.medicine.module.patient.service.examinations.ExaminationsService;
import com.sqlapk.medicine.module.patient.service.outpatientcheck.OutpatientCheckService;
import com.sqlapk.medicine.module.patient.service.outpatienteducation.OutpatientEducationService;
import com.sqlapk.medicine.module.patient.service.outpatientexam.OutpatientExamService;
import com.sqlapk.medicine.module.patient.service.outpatientmedicine.OutpatientMedicineService;
import com.sqlapk.medicine.module.patient.service.prescriptionexaminations.PrescriptionExaminationsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.OUTPATIENT_NOT_EXISTS;

/**
 * 门诊药学服务 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class OutpatientServiceImpl implements OutpatientService {

    @Resource
    private OutpatientMapper outpatientMapper;
    @Resource
    private PharmacyOutpatientMapper pharmacyOutpatientMapper;
    @Resource
    private EducationMapper educationMapper;
    @Resource
    private PrescriptionsMapper prescriptionsMapper;
    @Resource
    private OutpatientCheckMapper outpatientCheckMapper;
    @Resource
    private DrugsService drugsService;
    @Resource
    private PrescriptionExaminationsService prescriptionExaminationsService;
    @Resource
    private ExaminationsService examinationsService;
    @Resource
    private PrescriptionDetailMapper prescriptionDetailMapper;
    @Resource
    private OutpatientExamService outpatientExamService;
    @Resource
    private OutpatientExamMapper outpatientExamMapper;
    @Resource
    private OutpatientCheckService outpatientCheckService;
    @Resource
    private OutpatientEducationService outpatientEducationService;
    @Resource
    private OutpatientEducationMapper outpatientEducationMapper;
    @Resource
    private OutpatientMedicineService outpatientMedicineService;
    @Resource
    private OutpatientMedicineMapper outpatientMedicineMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOutpatient(OutpatientCreateReqVO createReqVO) {
        // 插入
        OutpatientDO outpatient = OutpatientConvert.INSTANCE.convert(createReqVO);
        outpatientMapper.insert(outpatient);

        createReqVO.getAudit().forEach(item -> {
            OutpatientCheckDO detailDO = OutpatientCheckConvert.INSTANCE.convert(item);
            detailDO.setId(null);
            detailDO.setOutpatientId(outpatient.getId());
            outpatientCheckMapper.insert(detailDO);
        });

        createReqVO.getEducation().forEach(item -> {
            OutpatientEducationDO edu = OutpatientEducationDO.builder()
                    .outpatientId(outpatient.getId())
                    .educationId(item.getId()).build();
            outpatientEducationMapper.insert(edu);
        });

        createReqVO.getMedicine().forEach(item -> {
            OutpatientMedicineDO medicineDO = OutpatientMedicineDO.builder()
                    .outpatientId(outpatient.getId())
                    .meicineId(item.getId()).build();
            outpatientMedicineMapper.insert(medicineDO);
        });

        createReqVO.getCheck().forEach(item -> {
            OutpatientExamDO examDO = OutpatientExamDO.builder()
                    .outpatientId(outpatient.getId())
                    .examinationsId(item.getId()).build();
            outpatientExamMapper.insert(examDO);
        });
        // 返回
        return outpatient.getId();
    }

    @Override
    public void updateOutpatient(OutpatientUpdateReqVO updateReqVO) {
        // 校验存在
        validateOutpatientExists(updateReqVO.getId());
        // 更新
        OutpatientDO updateObj = OutpatientConvert.INSTANCE.convert(updateReqVO);

        LambdaQueryWrapper<OutpatientExamDO> examDo = new LambdaQueryWrapper<OutpatientExamDO>().eq(OutpatientExamDO::getOutpatientId, updateReqVO.getId());
        outpatientExamMapper.delete(examDo);
        LambdaQueryWrapper<OutpatientCheckDO> checkDo = new LambdaQueryWrapper<OutpatientCheckDO>().eq(OutpatientCheckDO::getOutpatientId, updateReqVO.getId());
        outpatientCheckMapper.delete(checkDo);
        LambdaQueryWrapper<OutpatientMedicineDO> medicineDo = new LambdaQueryWrapper<OutpatientMedicineDO>().eq(OutpatientMedicineDO::getOutpatientId, updateReqVO.getId());
        outpatientMedicineMapper.delete(medicineDo);
        LambdaQueryWrapper<OutpatientEducationDO> educationDo = new LambdaQueryWrapper<OutpatientEducationDO>().eq(OutpatientEducationDO::getOutpatientId, updateReqVO.getId());
        outpatientEducationMapper.delete(educationDo);

        updateReqVO.getAudit().forEach(item -> {
            OutpatientCheckDO detailDO = OutpatientCheckConvert.INSTANCE.convert(item);
            detailDO.setId(null);
            detailDO.setOutpatientId(updateReqVO.getId());
            outpatientCheckMapper.insert(detailDO);
        });


        updateReqVO.getEducation().forEach(item -> {
            OutpatientEducationDO edu = OutpatientEducationDO.builder()
                    .outpatientId(updateReqVO.getId())
                    .educationId(item.getId()).build();
            outpatientEducationMapper.insert(edu);
        });

        updateReqVO.getMedicine().forEach(item -> {
            OutpatientMedicineDO medicineDO = OutpatientMedicineDO.builder()
                    .outpatientId(updateReqVO.getId())
                    .meicineId(item.getId()).build();
            outpatientMedicineMapper.insert(medicineDO);
        });

        updateReqVO.getCheck().forEach(item -> {
            OutpatientExamDO examDO = OutpatientExamDO.builder()
                    .outpatientId(updateReqVO.getId())
                    .examinationsId(item.getId()).build();
            outpatientExamMapper.insert(examDO);
        });

        outpatientMapper.updateById(updateObj);
    }

    @Override
    public void deleteOutpatient(Long id) {
        // 校验存在
        validateOutpatientExists(id);
        // 删除
        outpatientMapper.deleteById(id);
    }

    private void validateOutpatientExists(Long id) {
        if (outpatientMapper.selectById(id) == null) {
            throw exception(OUTPATIENT_NOT_EXISTS);
        }
    }

    @Override
    public OutpatientDO getOutpatient(Long id) {
        return outpatientMapper.selectById(id);
    }

    @Override
    public List<OutpatientDO> getOutpatientList(Collection<Long> ids) {
        return outpatientMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<OutpatientDO> getOutpatientPage(OutpatientPageReqVO pageReqVO) {
        return outpatientMapper.selectPage(pageReqVO);
    }

    @Override
    public List<OutpatientDO> getOutpatientList(OutpatientExportReqVO exportReqVO) {
        return outpatientMapper.selectList(exportReqVO);
    }

    @Override
    public OutpatientPatientInfoRespVO getPatientInfo(Long patientId) {
        LambdaQueryWrapperX<EducationDO> educationQuery = new LambdaQueryWrapperX<EducationDO>().eq(EducationDO::getPatientId, patientId);
        List<EducationDO> educationDOS = educationMapper.selectList(educationQuery);

        LambdaQueryWrapperX<PharmacyOutpatientDO> medicineQuery = new LambdaQueryWrapperX<PharmacyOutpatientDO>().eq(PharmacyOutpatientDO::getPatientId, patientId);
        List<PharmacyOutpatientDO> pharmacyOutpatientDOS = pharmacyOutpatientMapper.selectList(medicineQuery);

        PrescriptionsExportReqVO prescriptionsExportReqVO = new PrescriptionsExportReqVO();
        prescriptionsExportReqVO.setPatientId(patientId);
        List<PrescriptionsDO> prescriptionsDOS = prescriptionsMapper.selectList(prescriptionsExportReqVO);
        if (CollectionUtils.isEmpty(prescriptionsDOS)) {
            return OutpatientPatientInfoRespVO.builder().education(educationDOS).medicine(pharmacyOutpatientDOS).prescription(null).build();
        }

        PrescriptionsDO prescriptionsDO = prescriptionsDOS.get(0);
        PrescriptionDetailExportReqVO prescriptionDetailExportReqVO = new PrescriptionDetailExportReqVO();
        prescriptionDetailExportReqVO.setPrescriptionId(prescriptionsDO.getId());
        List<PrescriptionDetailDO> prescriptionDetailDOS = prescriptionDetailMapper.selectList(prescriptionDetailExportReqVO);
        List<PrescriptionDetailRespVO> prescriptionDetailRespVOS = PrescriptionDetailConvert.INSTANCE.convertList(prescriptionDetailDOS);
        prescriptionDetailRespVOS.forEach(item -> {
            DrugsDO drugsDO = drugsService.get(item.getMedicineId());
            item.setName(drugsDO.getDrugName());
            item.setMedicineName(drugsDO.getDrugName());
            item.setDrugId(item.getId());
            item.setDoctorId(prescriptionsDO.getDoctorId());
        });

        PrescriptionExaminationsExportReqVO examinationQuery = new PrescriptionExaminationsExportReqVO();
        examinationQuery.setPrescriptionId(prescriptionsDO.getId());
        List<PrescriptionExaminationsDO> examinationsList = prescriptionExaminationsService.getPrescriptionExaminationsList(examinationQuery);
        List<PrescriptionExaminationsRespVO> examinationsRespVOS = PrescriptionExaminationsConvert.INSTANCE.convertList(examinationsList);
        examinationsRespVOS.forEach(item -> {
            ExaminationsDO examinations = examinationsService.getExaminations(item.getExaminationId());
            item.setName(examinations.getName());
            item.setDescription(examinations.getDescription());
        });

        return OutpatientPatientInfoRespVO.builder().education(educationDOS)
                .medicine(pharmacyOutpatientDOS)
                .check(examinationsRespVOS)
                .prescription(prescriptionDetailRespVOS).build();
    }
}
