package com.sqlapk.medicine.module.patient.controller.admin.surveys;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;

import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;

import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import static com.sqlapk.medicine.framework.operatelog.core.enums.OperateTypeEnum.*;

import com.sqlapk.medicine.module.patient.controller.admin.surveys.vo.*;
import com.sqlapk.medicine.module.patient.dal.dataobject.surveys.SurveysDO;
import com.sqlapk.medicine.module.patient.convert.surveys.SurveysConvert;
import com.sqlapk.medicine.module.patient.service.surveys.SurveysService;

@Tag(name = "管理后台 - 问卷")
@RestController
@RequestMapping("/patient/surveys")
@Validated
public class SurveysController {

    @Resource
    private SurveysService surveysService;

    @PostMapping("/create")
    @Operation(summary = "创建问卷")
    @PreAuthorize("@ss.hasPermission('patient:surveys:create')")
    public CommonResult<Long> createSurveys(@Valid @RequestBody SurveysCreateReqVO createReqVO) {
        return success(surveysService.createSurveys(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新问卷")
    @PreAuthorize("@ss.hasPermission('patient:surveys:update')")
    public CommonResult<Boolean> updateSurveys(@Valid @RequestBody SurveysUpdateReqVO updateReqVO) {
        surveysService.updateSurveys(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除问卷")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:surveys:delete')")
    public CommonResult<Boolean> deleteSurveys(@RequestParam("id") Long id) {
        surveysService.deleteSurveys(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得问卷")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:surveys:query')")
    public CommonResult<SurveysRespVO> getSurveys(@RequestParam("id") Long id) {
        SurveysDO surveys = surveysService.getSurveys(id);
        return success(SurveysConvert.INSTANCE.convert(surveys));
    }

    @GetMapping("/list")
    @Operation(summary = "获得问卷列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:surveys:query')")
    public CommonResult<List<SurveysRespVO>> getSurveysList(@RequestParam("ids") Collection<Long> ids) {
        List<SurveysDO> list = surveysService.getSurveysList(ids);
        return success(SurveysConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得问卷分页")
    @PreAuthorize("@ss.hasPermission('patient:surveys:query')")
    public CommonResult<PageResult<SurveysRespVO>> getSurveysPage(@Valid SurveysPageReqVO pageVO) {
        PageResult<SurveysDO> pageResult = surveysService.getSurveysPage(pageVO);
        return success(SurveysConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出问卷 Excel")
    @PreAuthorize("@ss.hasPermission('patient:surveys:export')")
    @OperateLog(type = EXPORT)
    public void exportSurveysExcel(@Valid SurveysExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<SurveysDO> list = surveysService.getSurveysList(exportReqVO);
        // 导出 Excel
        List<SurveysExcelVO> datas = SurveysConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "问卷.xls", "数据", SurveysExcelVO.class, datas);
    }

}
