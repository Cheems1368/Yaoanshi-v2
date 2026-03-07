package com.sqlapk.medicine.module.patient.controller.admin.inpatient;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientUpdateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundExportReqVO;
import com.sqlapk.medicine.module.patient.convert.inpatient.InpatientConvert;
import com.sqlapk.medicine.module.patient.convert.inpatientadvice.InpatientAdviceConvert;
import com.sqlapk.medicine.module.patient.convert.inpatientconsultation.InpatientConsultationConvert;
import com.sqlapk.medicine.module.patient.convert.inpatienteducation.InpatientEducationConvert;
import com.sqlapk.medicine.module.patient.convert.inpatientevaluation.InpatientEvaluationConvert;
import com.sqlapk.medicine.module.patient.convert.inpatientround.InpatientRoundConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatient.InpatientDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientadvice.InpatientAdviceDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientconsultation.InpatientConsultationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatienteducation.InpatientEducationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientevaluation.InpatientEvaluationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientround.InpatientRoundDO;
import com.sqlapk.medicine.module.patient.service.inpatient.InpatientService;
import com.sqlapk.medicine.module.patient.service.inpatientadvice.InpatientAdviceService;
import com.sqlapk.medicine.module.patient.service.inpatientconsultation.InpatientConsultationService;
import com.sqlapk.medicine.module.patient.service.inpatienteducation.InpatientEducationService;
import com.sqlapk.medicine.module.patient.service.inpatientevaluation.InpatientEvaluationService;
import com.sqlapk.medicine.module.patient.service.inpatientround.InpatientRoundService;
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
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;
import static com.sqlapk.medicine.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 住院药学服务")
@RestController
@RequestMapping("/patient/inpatient")
@Validated
public class InpatientController {

    @Resource
    private InpatientService inpatientService;
    @Resource
    private InpatientConsultationService consultationService;
    @Resource
    private InpatientEducationService educationService;
    @Resource
    private InpatientEvaluationService evaluationService;
    @Resource
    private InpatientRoundService inpatientRoundService;
    @Resource
    private InpatientAdviceService inpatientAdviceService;

    @PostMapping("/create")
    @Operation(summary = "创建住院药学服务")
    @PreAuthorize("@ss.hasPermission('patient:inpatient:create')")
    public CommonResult<Long> createInpatient(@Valid @RequestBody InpatientCreateReqVO createReqVO) {
        return success(inpatientService.createInpatient(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新住院药学服务")
    @PreAuthorize("@ss.hasPermission('patient:inpatient:update')")
    public CommonResult<Boolean> updateInpatient(@Valid @RequestBody InpatientUpdateReqVO updateReqVO) {
        inpatientService.updateInpatient(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除住院药学服务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:inpatient:delete')")
    public CommonResult<Boolean> deleteInpatient(@RequestParam("id") Long id) {
        inpatientService.deleteInpatient(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得住院药学服务")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:inpatient:query')")
    public CommonResult<InpatientRespVO> getInpatient(@RequestParam("id") Long id) {
        InpatientDO inpatient = inpatientService.getInpatient(id);
        InpatientRespVO result = InpatientConvert.INSTANCE.convert(inpatient);
        //advice
        InpatientAdviceExportReqVO inpatientAdviceExportReqVO = new InpatientAdviceExportReqVO();
        inpatientAdviceExportReqVO.setInpatientId(id);
        List<InpatientAdviceDO> inpatientAdviceList = inpatientAdviceService.getInpatientAdviceList(inpatientAdviceExportReqVO);
        //education
        InpatientEducationExportReqVO educationExportReqVO = new InpatientEducationExportReqVO();
        educationExportReqVO.setInpatientId(id);
        List<InpatientEducationDO> inpatientEducationList = educationService.getInpatientEducationList(educationExportReqVO);
        //evaluation
        InpatientEvaluationExportReqVO evaluationExportReqVO = new InpatientEvaluationExportReqVO();
        evaluationExportReqVO.setInpatientId(id);
        List<InpatientEvaluationDO> inpatientEvaluationList = evaluationService.getInpatientEvaluationList(evaluationExportReqVO);
        //consultation
        InpatientConsultationExportReqVO consultationExportReqVO = new InpatientConsultationExportReqVO();
        consultationExportReqVO.setInpatientId(id);
        List<InpatientConsultationDO> inpatientConsultationList = consultationService.getInpatientConsultationList(consultationExportReqVO);
        //round
        InpatientRoundExportReqVO roundExportReqVO = new InpatientRoundExportReqVO();
        roundExportReqVO.setInpatientId(id);
        List<InpatientRoundDO> inpatientRoundList = inpatientRoundService.getInpatientRoundList(roundExportReqVO);

        result.setConsultation(InpatientConsultationConvert.INSTANCE.convertList(inpatientConsultationList));
        result.setEducation(InpatientEducationConvert.INSTANCE.convertList(inpatientEducationList));
        result.setRound(InpatientRoundConvert.INSTANCE.convertList(inpatientRoundList));
        result.setAdvice(InpatientAdviceConvert.INSTANCE.convertList(inpatientAdviceList));
        result.setEvaluation(InpatientEvaluationConvert.INSTANCE.convertList(inpatientEvaluationList));
        return success(result);
    }

    @GetMapping("/list")
    @Operation(summary = "获得住院药学服务列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:inpatient:query')")
    public CommonResult<List<InpatientRespVO>> getInpatientList(@RequestParam("ids") Collection<Long> ids) {
        List<InpatientDO> list = inpatientService.getInpatientList(ids);
        return success(InpatientConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得住院药学服务分页")
    @PreAuthorize("@ss.hasPermission('patient:inpatient:query')")
    public CommonResult<PageResult<InpatientRespVO>> getInpatientPage(@Valid InpatientPageReqVO pageVO) {
        PageResult<InpatientDO> pageResult = inpatientService.getInpatientPage(pageVO);
        return success(InpatientConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出住院药学服务 Excel")
    @PreAuthorize("@ss.hasPermission('patient:inpatient:export')")
    @OperateLog(type = EXPORT)
    public void exportInpatientExcel(@Valid InpatientExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<InpatientDO> list = inpatientService.getInpatientList(exportReqVO);
        // 导出 Excel
        List<InpatientExcelVO> datas = InpatientConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "住院药学服务.xls", "数据", InpatientExcelVO.class, datas);
    }

}
