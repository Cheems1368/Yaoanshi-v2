package com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.inpatienteducation.InpatientEducationConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatienteducation.InpatientEducationDO;
import com.sqlapk.medicine.module.patient.service.inpatienteducation.InpatientEducationService;
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

@Tag(name = "管理后台 - 住院用药教育")
@RestController
@RequestMapping("/patient/inpatient-education")
@Validated
public class InpatientEducationController {

    @Resource
    private InpatientEducationService inpatientEducationService;

    @PostMapping("/create")
    @Operation(summary = "创建住院用药教育")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-education:create')")
    public CommonResult<Long> createInpatientEducation(@Valid @RequestBody InpatientEducationCreateReqVO createReqVO) {
        return success(inpatientEducationService.createInpatientEducation(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新住院用药教育")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-education:update')")
    public CommonResult<Boolean> updateInpatientEducation(@Valid @RequestBody InpatientEducationUpdateReqVO updateReqVO) {
        inpatientEducationService.updateInpatientEducation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除住院用药教育")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:inpatient-education:delete')")
    public CommonResult<Boolean> deleteInpatientEducation(@RequestParam("id") Long id) {
        inpatientEducationService.deleteInpatientEducation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得住院用药教育")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-education:query')")
    public CommonResult<InpatientEducationRespVO> getInpatientEducation(@RequestParam("id") Long id) {
        InpatientEducationDO inpatientEducation = inpatientEducationService.getInpatientEducation(id);
        return success(InpatientEducationConvert.INSTANCE.convert(inpatientEducation));
    }

    @GetMapping("/list")
    @Operation(summary = "获得住院用药教育列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-education:query')")
    public CommonResult<List<InpatientEducationRespVO>> getInpatientEducationList(@RequestParam("ids") Collection<Long> ids) {
        List<InpatientEducationDO> list = inpatientEducationService.getInpatientEducationList(ids);
        return success(InpatientEducationConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得住院用药教育分页")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-education:query')")
    public CommonResult<PageResult<InpatientEducationRespVO>> getInpatientEducationPage(@Valid InpatientEducationPageReqVO pageVO) {
        PageResult<InpatientEducationDO> pageResult = inpatientEducationService.getInpatientEducationPage(pageVO);
        return success(InpatientEducationConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出住院用药教育 Excel")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-education:export')")
    @OperateLog(type = EXPORT)
    public void exportInpatientEducationExcel(@Valid InpatientEducationExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<InpatientEducationDO> list = inpatientEducationService.getInpatientEducationList(exportReqVO);
        // 导出 Excel
        List<InpatientEducationExcelVO> datas = InpatientEducationConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "住院用药教育.xls", "数据", InpatientEducationExcelVO.class, datas);
    }

}
