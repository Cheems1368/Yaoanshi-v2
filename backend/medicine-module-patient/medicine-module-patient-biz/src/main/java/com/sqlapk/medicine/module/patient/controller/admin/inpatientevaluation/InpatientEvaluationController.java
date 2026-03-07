package com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.inpatientevaluation.InpatientEvaluationConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientevaluation.InpatientEvaluationDO;
import com.sqlapk.medicine.module.patient.service.inpatientevaluation.InpatientEvaluationService;
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

@Tag(name = "管理后台 - 住院药学用药评估")
@RestController
@RequestMapping("/patient/inpatient-evaluation")
@Validated
public class InpatientEvaluationController {

    @Resource
    private InpatientEvaluationService inpatientEvaluationService;

    @PostMapping("/create")
    @Operation(summary = "创建住院药学用药评估")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-evaluation:create')")
    public CommonResult<Long> createInpatientEvaluation(@Valid @RequestBody InpatientEvaluationCreateReqVO createReqVO) {
        return success(inpatientEvaluationService.createInpatientEvaluation(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新住院药学用药评估")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-evaluation:update')")
    public CommonResult<Boolean> updateInpatientEvaluation(@Valid @RequestBody InpatientEvaluationUpdateReqVO updateReqVO) {
        inpatientEvaluationService.updateInpatientEvaluation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除住院药学用药评估")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:inpatient-evaluation:delete')")
    public CommonResult<Boolean> deleteInpatientEvaluation(@RequestParam("id") Long id) {
        inpatientEvaluationService.deleteInpatientEvaluation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得住院药学用药评估")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-evaluation:query')")
    public CommonResult<InpatientEvaluationRespVO> getInpatientEvaluation(@RequestParam("id") Long id) {
        InpatientEvaluationDO inpatientEvaluation = inpatientEvaluationService.getInpatientEvaluation(id);
        return success(InpatientEvaluationConvert.INSTANCE.convert(inpatientEvaluation));
    }

    @GetMapping("/list")
    @Operation(summary = "获得住院药学用药评估列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-evaluation:query')")
    public CommonResult<List<InpatientEvaluationRespVO>> getInpatientEvaluationList(@RequestParam("ids") Collection<Long> ids) {
        List<InpatientEvaluationDO> list = inpatientEvaluationService.getInpatientEvaluationList(ids);
        return success(InpatientEvaluationConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得住院药学用药评估分页")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-evaluation:query')")
    public CommonResult<PageResult<InpatientEvaluationRespVO>> getInpatientEvaluationPage(@Valid InpatientEvaluationPageReqVO pageVO) {
        PageResult<InpatientEvaluationDO> pageResult = inpatientEvaluationService.getInpatientEvaluationPage(pageVO);
        return success(InpatientEvaluationConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出住院药学用药评估 Excel")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-evaluation:export')")
    @OperateLog(type = EXPORT)
    public void exportInpatientEvaluationExcel(@Valid InpatientEvaluationExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<InpatientEvaluationDO> list = inpatientEvaluationService.getInpatientEvaluationList(exportReqVO);
        // 导出 Excel
        List<InpatientEvaluationExcelVO> datas = InpatientEvaluationConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "住院药学用药评估.xls", "数据", InpatientEvaluationExcelVO.class, datas);
    }

}
