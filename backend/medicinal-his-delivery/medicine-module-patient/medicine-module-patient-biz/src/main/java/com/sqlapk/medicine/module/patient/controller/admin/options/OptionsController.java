package com.sqlapk.medicine.module.patient.controller.admin.options;

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

import com.sqlapk.medicine.module.patient.controller.admin.options.vo.*;
import com.sqlapk.medicine.module.patient.dal.dataobject.options.OptionsDO;
import com.sqlapk.medicine.module.patient.convert.options.OptionsConvert;
import com.sqlapk.medicine.module.patient.service.options.OptionsService;

@Tag(name = "管理后台 - 问题选项")
@RestController
@RequestMapping("/patient/options")
@Validated
public class OptionsController {

    @Resource
    private OptionsService optionsService;

    @PostMapping("/create")
    @Operation(summary = "创建问题选项")
    @PreAuthorize("@ss.hasPermission('patient:options:create')")
    public CommonResult<Long> createOptions(@Valid @RequestBody OptionsCreateReqVO createReqVO) {
        return success(optionsService.createOptions(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新问题选项")
    @PreAuthorize("@ss.hasPermission('patient:options:update')")
    public CommonResult<Boolean> updateOptions(@Valid @RequestBody OptionsUpdateReqVO updateReqVO) {
        optionsService.updateOptions(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除问题选项")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:options:delete')")
    public CommonResult<Boolean> deleteOptions(@RequestParam("id") Long id) {
        optionsService.deleteOptions(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得问题选项")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:options:query')")
    public CommonResult<OptionsRespVO> getOptions(@RequestParam("id") Long id) {
        OptionsDO options = optionsService.getOptions(id);
        return success(OptionsConvert.INSTANCE.convert(options));
    }

    @GetMapping("/list")
    @Operation(summary = "获得问题选项列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:options:query')")
    public CommonResult<List<OptionsRespVO>> getOptionsList(@RequestParam("ids") Collection<Long> ids) {
        List<OptionsDO> list = optionsService.getOptionsList(ids);
        return success(OptionsConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得问题选项分页")
    @PreAuthorize("@ss.hasPermission('patient:options:query')")
    public CommonResult<PageResult<OptionsRespVO>> getOptionsPage(@Valid OptionsPageReqVO pageVO) {
        PageResult<OptionsDO> pageResult = optionsService.getOptionsPage(pageVO);
        return success(OptionsConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出问题选项 Excel")
    @PreAuthorize("@ss.hasPermission('patient:options:export')")
    @OperateLog(type = EXPORT)
    public void exportOptionsExcel(@Valid OptionsExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<OptionsDO> list = optionsService.getOptionsList(exportReqVO);
        // 导出 Excel
        List<OptionsExcelVO> datas = OptionsConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "问题选项.xls", "数据", OptionsExcelVO.class, datas);
    }

}
