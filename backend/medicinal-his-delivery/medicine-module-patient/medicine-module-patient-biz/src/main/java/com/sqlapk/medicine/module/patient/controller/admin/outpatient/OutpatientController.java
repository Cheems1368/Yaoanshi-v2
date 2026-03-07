package com.sqlapk.medicine.module.patient.controller.admin.outpatient;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientPatientInfoRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientUpdateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsSimpleRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsRespVO;
import com.sqlapk.medicine.module.patient.convert.outpatient.OutpatientConvert;
import com.sqlapk.medicine.module.patient.convert.prescriptionexaminations.PrescriptionExaminationsConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.education.EducationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.examinations.ExaminationsDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatient.OutpatientDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientcheck.OutpatientCheckDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatienteducation.OutpatientEducationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientexam.OutpatientExamDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientmedicine.OutpatientMedicineDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.pharmacyoutpatient.PharmacyOutpatientDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptionexaminations.PrescriptionExaminationsDO;
import com.sqlapk.medicine.module.patient.dal.mysql.outpatientcheck.OutpatientCheckMapper;
import com.sqlapk.medicine.module.patient.service.education.EducationService;
import com.sqlapk.medicine.module.patient.service.examinations.ExaminationsService;
import com.sqlapk.medicine.module.patient.service.outpatient.OutpatientService;
import com.sqlapk.medicine.module.patient.service.outpatienteducation.OutpatientEducationService;
import com.sqlapk.medicine.module.patient.service.outpatientexam.OutpatientExamService;
import com.sqlapk.medicine.module.patient.service.outpatientmedicine.OutpatientMedicineService;
import com.sqlapk.medicine.module.patient.service.patient.PatientsService;
import com.sqlapk.medicine.module.patient.service.pharmacyoutpatient.PharmacyOutpatientService;
import com.sqlapk.medicine.module.patient.service.prescriptionexaminations.PrescriptionExaminationsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;
import static com.sqlapk.medicine.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 门诊药学服务")
@RestController
@RequestMapping("/patient/outpatient")
@Validated
public class OutpatientController {

    @Resource
    private OutpatientService outpatientService;
    @Resource
    private OutpatientExamService outpatientExamService;
    @Resource
    private OutpatientCheckMapper outpatientCheckMapper;
    @Resource
    private OutpatientEducationService outpatientEducationService;
    @Resource
    private OutpatientMedicineService outpatientMedicineService;
    @Resource
    private PrescriptionExaminationsService prescriptionExaminationsService;
    @Resource
    private EducationService educationService;
    @Resource
    private ExaminationsService examinationsService;
    @Resource
    private PharmacyOutpatientService pharmacyOutpatientService;
    @Resource
    private PatientsService patientsService;


    @PostMapping("/create")
    @Operation(summary = "创建门诊药学服务")
    @PreAuthorize("@ss.hasPermission('patient:outpatient:create')")
    public CommonResult<Long> createOutpatient(@Valid @RequestBody OutpatientCreateReqVO createReqVO) {
        return success(outpatientService.createOutpatient(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新门诊药学服务")
    @PreAuthorize("@ss.hasPermission('patient:outpatient:update')")
    public CommonResult<Boolean> updateOutpatient(@Valid @RequestBody OutpatientUpdateReqVO updateReqVO) {
        outpatientService.updateOutpatient(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除门诊药学服务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:outpatient:delete')")
    public CommonResult<Boolean> deleteOutpatient(@RequestParam("id") Long id) {
        outpatientService.deleteOutpatient(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得门诊药学服务")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:outpatient:query')")
    public CommonResult<OutpatientRespVO> getOutpatient(@RequestParam("id") Long id) {
        OutpatientDO outpatient = outpatientService.getOutpatient(id);
        OutpatientRespVO response = OutpatientConvert.INSTANCE.convert(outpatient);
        //1. outpatient check
        OutpatientCheckExportReqVO outpatientCheckExportReqVO = new OutpatientCheckExportReqVO();
        outpatientCheckExportReqVO.setOutpatientId(id);
        List<OutpatientCheckDO> outpatientCheckDOS = outpatientCheckMapper.selectList(outpatientCheckExportReqVO);
        //2 outpatient exam
        OutpatientExamExportReqVO examExportReqVO = new OutpatientExamExportReqVO();
        examExportReqVO.setOutpatientId(id);
        List<OutpatientExamDO> outpatientExamList = outpatientExamService.getOutpatientExamList(examExportReqVO);
        List<PrescriptionExaminationsDO> examinationsList = prescriptionExaminationsService.getPrescriptionExaminationsList(outpatientExamList.stream().map(OutpatientExamDO::getExaminationsId).collect(Collectors.toList()));
        List<PrescriptionExaminationsRespVO> examinationsRespVOS = PrescriptionExaminationsConvert.INSTANCE.convertList(examinationsList);
        examinationsRespVOS.forEach(item -> {
            ExaminationsDO examinations = examinationsService.getExaminations(item.getExaminationId());
            item.setName(examinations.getName());
        });
        //3. outpatient education
        OutpatientEducationExportReqVO outpatientEducationExportReqVO = new OutpatientEducationExportReqVO();
        outpatientEducationExportReqVO.setOutpatientId(id);
        List<OutpatientEducationDO> outpatientEducationList = outpatientEducationService.getOutpatientEducationList(outpatientEducationExportReqVO);
        List<EducationDO> eductionList = educationService.getEductionList(outpatientEducationList.stream().map(OutpatientEducationDO::getEducationId).collect(Collectors.toList()));
        //4. outpatient medicine
        OutpatientMedicineExportReqVO outpatientMedicineExportReqVO = new OutpatientMedicineExportReqVO();
        outpatientMedicineExportReqVO.setOutpatientId(id);
        List<OutpatientMedicineDO> outpatientMedicineList = outpatientMedicineService.getOutpatientMedicineList(outpatientMedicineExportReqVO);
        List<PharmacyOutpatientDO> pharmacyOutpatientList = pharmacyOutpatientService.getPharmacyOutpatientList(outpatientMedicineList.stream().map(OutpatientMedicineDO::getMeicineId).collect(Collectors.toList()));
        response.setAudit(outpatientCheckDOS);
        response.setMedicine(pharmacyOutpatientList);
        response.setEducation(eductionList);
        response.setCheck(examinationsRespVOS);
        //4. patient info
        List<PatientsSimpleRespVO> patientSimple = patientsService.getPatientSimple();
        Optional<PatientsSimpleRespVO> patientInfo = patientSimple.stream().filter(item -> response.getPatientId().equals(item.getId())).findFirst();
        response.setPatient(patientInfo.orElse(null));
        return success(response);
    }

    @GetMapping("/list")
    @Operation(summary = "获得门诊药学服务列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:outpatient:query')")
    public CommonResult<List<OutpatientRespVO>> getOutpatientList(@RequestParam("ids") Collection<Long> ids) {
        List<OutpatientDO> list = outpatientService.getOutpatientList(ids);
        return success(OutpatientConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得门诊药学服务分页")
    @PreAuthorize("@ss.hasPermission('patient:outpatient:query')")
    public CommonResult<PageResult<OutpatientRespVO>> getOutpatientPage(@Valid OutpatientPageReqVO pageVO) {
        PageResult<OutpatientDO> pageResult = outpatientService.getOutpatientPage(pageVO);
        return success(OutpatientConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/detail")
    @Operation(summary = "患者详解")
    @PreAuthorize("@ss.hasPermission('patient:outpatient:query')")
    public CommonResult<OutpatientPatientInfoRespVO> getPatientInfo(@Valid @NotNull Long patientId) {
        OutpatientPatientInfoRespVO result = outpatientService.getPatientInfo(patientId);
        return success(result);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出门诊药学服务 Excel")
    @PreAuthorize("@ss.hasPermission('patient:outpatient:export')")
    @OperateLog(type = EXPORT)
    public void exportOutpatientExcel(@Valid OutpatientExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<OutpatientDO> list = outpatientService.getOutpatientList(exportReqVO);
        // 导出 Excel
        List<OutpatientExcelVO> datas = OutpatientConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "门诊药学服务.xls", "数据", OutpatientExcelVO.class, datas);
    }

}
