package com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.outpatientcheck.OutpatientCheckConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientcheck.OutpatientCheckDO;
import com.sqlapk.medicine.module.patient.service.outpatientcheck.OutpatientCheckService;
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

@Tag(name = "管理后台 - 处方审核")
@RestController
@RequestMapping("/patient/outpatient-check")
@Validated
public class OutpatientCheckController {

    @Resource
    private OutpatientCheckService outpatientCheckService;

    @PostMapping("/create")
    @Operation(summary = "创建处方审核")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-check:create')")
    public CommonResult<Long> createOutpatientCheck(@Valid @RequestBody OutpatientCheckCreateReqVO createReqVO) {
        return success(outpatientCheckService.createOutpatientCheck(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新处方审核")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-check:update')")
    public CommonResult<Boolean> updateOutpatientCheck(@Valid @RequestBody OutpatientCheckUpdateReqVO updateReqVO) {
        outpatientCheckService.updateOutpatientCheck(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除处方审核")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:outpatient-check:delete')")
    public CommonResult<Boolean> deleteOutpatientCheck(@RequestParam("id") Long id) {
        outpatientCheckService.deleteOutpatientCheck(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得处方审核")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-check:query')")
    public CommonResult<OutpatientCheckRespVO> getOutpatientCheck(@RequestParam("id") Long id) {
        OutpatientCheckDO outpatientCheck = outpatientCheckService.getOutpatientCheck(id);
        return success(OutpatientCheckConvert.INSTANCE.convert(outpatientCheck));
    }

    @GetMapping("/list")
    @Operation(summary = "获得处方审核列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-check:query')")
    public CommonResult<List<OutpatientCheckRespVO>> getOutpatientCheckList(@RequestParam("ids") Collection<Long> ids) {
        List<OutpatientCheckDO> list = outpatientCheckService.getOutpatientCheckList(ids);
        return success(OutpatientCheckConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得处方审核分页")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-check:query')")
    public CommonResult<PageResult<OutpatientCheckRespVO>> getOutpatientCheckPage(@Valid OutpatientCheckPageReqVO pageVO) {
        PageResult<OutpatientCheckDO> pageResult = outpatientCheckService.getOutpatientCheckPage(pageVO);
        return success(OutpatientCheckConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出处方审核 Excel")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-check:export')")
    @OperateLog(type = EXPORT)
    public void exportOutpatientCheckExcel(@Valid OutpatientCheckExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<OutpatientCheckDO> list = outpatientCheckService.getOutpatientCheckList(exportReqVO);
        // 导出 Excel
        List<OutpatientCheckExcelVO> datas = OutpatientCheckConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "处方审核.xls", "数据", OutpatientCheckExcelVO.class, datas);
    }

}
