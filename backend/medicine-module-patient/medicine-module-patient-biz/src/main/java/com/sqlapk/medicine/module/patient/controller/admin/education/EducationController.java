package com.sqlapk.medicine.module.patient.controller.admin.education;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.eduction.EductionConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.education.EducationDO;
import com.sqlapk.medicine.module.patient.service.education.EducationService;
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

@Tag(name = "管理后台 - 用药教育")
@RestController
@RequestMapping("/patient/education")
@Validated
public class EducationController {

    @Resource
    private EducationService eductionService;

    @PostMapping("/create")
    @Operation(summary = "创建用药教育")
    @PreAuthorize("@ss.hasPermission('patient:education:create')")
    public CommonResult<Long> createEduction(@Valid @RequestBody EducationCreateReqVO createReqVO) {
        return success(eductionService.createEduction(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用药教育")
    @PreAuthorize("@ss.hasPermission('patient:education:update')")
    public CommonResult<Boolean> updateEduction(@Valid @RequestBody EducationUpdateReqVO updateReqVO) {
        eductionService.updateEduction(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用药教育")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:education:delete')")
    public CommonResult<Boolean> deleteEduction(@RequestParam("id") Long id) {
        eductionService.deleteEduction(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用药教育")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:education:query')")
    public CommonResult<EducationRespVO> getEduction(@RequestParam("id") Long id) {
        EducationDO eduction = eductionService.getEduction(id);
        return success(EductionConvert.INSTANCE.convert(eduction));
    }

    @GetMapping("/list")
    @Operation(summary = "获得用药教育列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:education:query')")
    public CommonResult<List<EducationRespVO>> getEductionList(@RequestParam("ids") Collection<Long> ids) {
        List<EducationDO> list = eductionService.getEductionList(ids);
        return success(EductionConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用药教育分页")
    @PreAuthorize("@ss.hasPermission('patient:education:query')")
    public CommonResult<PageResult<EducationRespVO>> getEductionPage(@Valid EducationPageReqVO pageVO) {
        PageResult<EducationDO> pageResult = eductionService.getEductionPage(pageVO);
        return success(EductionConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出用药教育 Excel")
    @PreAuthorize("@ss.hasPermission('patient:education:export')")
    @OperateLog(type = EXPORT)
    public void exportEductionExcel(@Valid EducationExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<EducationDO> list = eductionService.getEductionList(exportReqVO);
        // 导出 Excel
        List<EducationExcelVO> datas = EductionConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "用药教育.xls", "数据", EducationExcelVO.class, datas);
    }

}
