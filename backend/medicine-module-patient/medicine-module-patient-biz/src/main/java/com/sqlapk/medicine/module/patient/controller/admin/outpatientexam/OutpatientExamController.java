package com.sqlapk.medicine.module.patient.controller.admin.outpatientexam;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.outpatientexam.OutpatientExamConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientexam.OutpatientExamDO;
import com.sqlapk.medicine.module.patient.service.outpatientexam.OutpatientExamService;
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

@Tag(name = "管理后台 - 门诊药学检查项目明细")
@RestController
@RequestMapping("/patient/outpatient-exam")
@Validated
public class OutpatientExamController {

    @Resource
    private OutpatientExamService outpatientExamService;

    @PostMapping("/create")
    @Operation(summary = "创建门诊药学检查项目明细")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-exam:create')")
    public CommonResult<Long> createOutpatientExam(@Valid @RequestBody OutpatientExamCreateReqVO createReqVO) {
        return success(outpatientExamService.createOutpatientExam(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新门诊药学检查项目明细")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-exam:update')")
    public CommonResult<Boolean> updateOutpatientExam(@Valid @RequestBody OutpatientExamUpdateReqVO updateReqVO) {
        outpatientExamService.updateOutpatientExam(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除门诊药学检查项目明细")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:outpatient-exam:delete')")
    public CommonResult<Boolean> deleteOutpatientExam(@RequestParam("id") Long id) {
        outpatientExamService.deleteOutpatientExam(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得门诊药学检查项目明细")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-exam:query')")
    public CommonResult<OutpatientExamRespVO> getOutpatientExam(@RequestParam("id") Long id) {
        OutpatientExamDO outpatientExam = outpatientExamService.getOutpatientExam(id);
        return success(OutpatientExamConvert.INSTANCE.convert(outpatientExam));
    }

    @GetMapping("/list")
    @Operation(summary = "获得门诊药学检查项目明细列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-exam:query')")
    public CommonResult<List<OutpatientExamRespVO>> getOutpatientExamList(@RequestParam("ids") Collection<Long> ids) {
        List<OutpatientExamDO> list = outpatientExamService.getOutpatientExamList(ids);
        return success(OutpatientExamConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得门诊药学检查项目明细分页")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-exam:query')")
    public CommonResult<PageResult<OutpatientExamRespVO>> getOutpatientExamPage(@Valid OutpatientExamPageReqVO pageVO) {
        PageResult<OutpatientExamDO> pageResult = outpatientExamService.getOutpatientExamPage(pageVO);
        return success(OutpatientExamConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出门诊药学检查项目明细 Excel")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-exam:export')")
    @OperateLog(type = EXPORT)
    public void exportOutpatientExamExcel(@Valid OutpatientExamExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<OutpatientExamDO> list = outpatientExamService.getOutpatientExamList(exportReqVO);
        // 导出 Excel
        List<OutpatientExamExcelVO> datas = OutpatientExamConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "门诊药学检查项目明细.xls", "数据", OutpatientExamExcelVO.class, datas);
    }

}
