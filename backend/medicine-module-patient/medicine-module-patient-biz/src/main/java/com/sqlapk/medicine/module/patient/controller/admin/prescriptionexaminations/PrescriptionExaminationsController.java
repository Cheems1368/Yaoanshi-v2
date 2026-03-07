package com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.prescriptionexaminations.PrescriptionExaminationsConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.examinations.ExaminationsDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptionexaminations.PrescriptionExaminationsDO;
import com.sqlapk.medicine.module.patient.service.examinations.ExaminationsService;
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
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;
import static com.sqlapk.medicine.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 处方检查项目关系")
@RestController
@RequestMapping("/patient/prescription-examinations")
@Validated
public class PrescriptionExaminationsController {

    @Resource
    private PrescriptionExaminationsService prescriptionExaminationsService;
    @Resource
    private ExaminationsService examinationsService;

    @PostMapping("/create")
    @Operation(summary = "创建处方检查项目关系")
    @PreAuthorize("@ss.hasPermission('patient:prescription-examinations:create')")
    public CommonResult<Long> createPrescriptionExaminations(@Valid @RequestBody PrescriptionExaminationsCreateReqVO createReqVO) {
        return success(prescriptionExaminationsService.createPrescriptionExaminations(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新处方检查项目关系")
    @PreAuthorize("@ss.hasPermission('patient:prescription-examinations:update')")
    public CommonResult<Boolean> updatePrescriptionExaminations(@Valid @RequestBody PrescriptionExaminationsUpdateReqVO updateReqVO) {
        prescriptionExaminationsService.updatePrescriptionExaminations(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除处方检查项目关系")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:prescription-examinations:delete')")
    public CommonResult<Boolean> deletePrescriptionExaminations(@RequestParam("id") Long id) {
        prescriptionExaminationsService.deletePrescriptionExaminations(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得处方检查项目关系")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:prescription-examinations:query')")
    public CommonResult<PrescriptionExaminationsRespVO> getPrescriptionExaminations(@RequestParam("id") Long id) {
        PrescriptionExaminationsDO prescriptionExaminations = prescriptionExaminationsService.getPrescriptionExaminations(id);
        return success(PrescriptionExaminationsConvert.INSTANCE.convert(prescriptionExaminations));
    }

    @GetMapping("/list")
    @Operation(summary = "获得处方检查项目关系列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:prescription-examinations:query')")
    public CommonResult<List<PrescriptionExaminationsRespVO>> getPrescriptionExaminationsList(@RequestParam("ids") Collection<Long> ids) {
        List<PrescriptionExaminationsDO> list = prescriptionExaminationsService.getPrescriptionExaminationsList(ids);
        return success(PrescriptionExaminationsConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得处方检查项目关系分页")
    @PreAuthorize("@ss.hasPermission('patient:prescription-examinations:query')")
    public CommonResult<PageResult<PrescriptionExaminationsRespVO>> getPrescriptionExaminationsPage(@Valid PrescriptionExaminationsPageReqVO pageVO) {
        PageResult<PrescriptionExaminationsDO> pageResult = prescriptionExaminationsService.getPrescriptionExaminationsPage(pageVO);
        PageResult<PrescriptionExaminationsRespVO> result = PrescriptionExaminationsConvert.INSTANCE.convertPage(pageResult);
        result.getList().forEach(item -> {
            ExaminationsDO examinations = examinationsService.getExaminations(item.getExaminationId());
            item.setName(examinations.getName());
            item.setDescription(examinations.getDescription());
        });
        return success(result);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出处方检查项目关系 Excel")
    @PreAuthorize("@ss.hasPermission('patient:prescription-examinations:export')")
    @OperateLog(type = EXPORT)
    public void exportPrescriptionExaminationsExcel(@Valid PrescriptionExaminationsExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<PrescriptionExaminationsDO> list = prescriptionExaminationsService.getPrescriptionExaminationsList(exportReqVO);
        // 导出 Excel
        List<PrescriptionExaminationsExcelVO> datas = PrescriptionExaminationsConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "处方检查项目关系.xls", "数据", PrescriptionExaminationsExcelVO.class, datas);
    }

}
