package com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.outpatienteducation.OutpatientEducationConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatienteducation.OutpatientEducationDO;
import com.sqlapk.medicine.module.patient.service.outpatienteducation.OutpatientEducationService;
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

@Tag(name = "管理后台 - 门诊处方用药教育明细")
@RestController
@RequestMapping("/patient/outpatient-education")
@Validated
public class OutpatientEducationController {

    @Resource
    private OutpatientEducationService outpatientEducationService;

    @PostMapping("/create")
    @Operation(summary = "创建门诊处方用药教育明细")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-education:create')")
    public CommonResult<Long> createOutpatientEducation(@Valid @RequestBody OutpatientEducationCreateReqVO createReqVO) {
        return success(outpatientEducationService.createOutpatientEducation(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新门诊处方用药教育明细")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-education:update')")
    public CommonResult<Boolean> updateOutpatientEducation(@Valid @RequestBody OutpatientEducationUpdateReqVO updateReqVO) {
        outpatientEducationService.updateOutpatientEducation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除门诊处方用药教育明细")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:outpatient-education:delete')")
    public CommonResult<Boolean> deleteOutpatientEducation(@RequestParam("id") Long id) {
        outpatientEducationService.deleteOutpatientEducation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得门诊处方用药教育明细")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-education:query')")
    public CommonResult<OutpatientEducationRespVO> getOutpatientEducation(@RequestParam("id") Long id) {
        OutpatientEducationDO outpatientEducation = outpatientEducationService.getOutpatientEducation(id);
        return success(OutpatientEducationConvert.INSTANCE.convert(outpatientEducation));
    }

    @GetMapping("/list")
    @Operation(summary = "获得门诊处方用药教育明细列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-education:query')")
    public CommonResult<List<OutpatientEducationRespVO>> getOutpatientEducationList(@RequestParam("ids") Collection<Long> ids) {
        List<OutpatientEducationDO> list = outpatientEducationService.getOutpatientEducationList(ids);
        return success(OutpatientEducationConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得门诊处方用药教育明细分页")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-education:query')")
    public CommonResult<PageResult<OutpatientEducationRespVO>> getOutpatientEducationPage(@Valid OutpatientEducationPageReqVO pageVO) {
        PageResult<OutpatientEducationDO> pageResult = outpatientEducationService.getOutpatientEducationPage(pageVO);
        return success(OutpatientEducationConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出门诊处方用药教育明细 Excel")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-education:export')")
    @OperateLog(type = EXPORT)
    public void exportOutpatientEducationExcel(@Valid OutpatientEducationExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<OutpatientEducationDO> list = outpatientEducationService.getOutpatientEducationList(exportReqVO);
        // 导出 Excel
        List<OutpatientEducationExcelVO> datas = OutpatientEducationConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "门诊处方用药教育明细.xls", "数据", OutpatientEducationExcelVO.class, datas);
    }

}
