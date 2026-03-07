package com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdvicePageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.inpatientadvice.InpatientAdviceConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientadvice.InpatientAdviceDO;
import com.sqlapk.medicine.module.patient.service.inpatientadvice.InpatientAdviceService;
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

@Tag(name = "管理后台 - 住院药学不合理用药")
@RestController
@RequestMapping("/patient/inpatient-advice")
@Validated
public class InpatientAdviceController {

    @Resource
    private InpatientAdviceService inpatientAdviceService;

    @PostMapping("/create")
    @Operation(summary = "创建住院药学不合理用药")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-advice:create')")
    public CommonResult<Long> createInpatientAdvice(@Valid @RequestBody InpatientAdviceCreateReqVO createReqVO) {
        return success(inpatientAdviceService.createInpatientAdvice(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新住院药学不合理用药")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-advice:update')")
    public CommonResult<Boolean> updateInpatientAdvice(@Valid @RequestBody InpatientAdviceUpdateReqVO updateReqVO) {
        inpatientAdviceService.updateInpatientAdvice(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除住院药学不合理用药")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:inpatient-advice:delete')")
    public CommonResult<Boolean> deleteInpatientAdvice(@RequestParam("id") Long id) {
        inpatientAdviceService.deleteInpatientAdvice(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得住院药学不合理用药")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-advice:query')")
    public CommonResult<InpatientAdviceRespVO> getInpatientAdvice(@RequestParam("id") Long id) {
        InpatientAdviceDO inpatientAdvice = inpatientAdviceService.getInpatientAdvice(id);
        return success(InpatientAdviceConvert.INSTANCE.convert(inpatientAdvice));
    }

    @GetMapping("/list")
    @Operation(summary = "获得住院药学不合理用药列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-advice:query')")
    public CommonResult<List<InpatientAdviceRespVO>> getInpatientAdviceList(@RequestParam("ids") Collection<Long> ids) {
        List<InpatientAdviceDO> list = inpatientAdviceService.getInpatientAdviceList(ids);
        return success(InpatientAdviceConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得住院药学不合理用药分页")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-advice:query')")
    public CommonResult<PageResult<InpatientAdviceRespVO>> getInpatientAdvicePage(@Valid InpatientAdvicePageReqVO pageVO) {
        PageResult<InpatientAdviceDO> pageResult = inpatientAdviceService.getInpatientAdvicePage(pageVO);
        return success(InpatientAdviceConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出住院药学不合理用药 Excel")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-advice:export')")
    @OperateLog(type = EXPORT)
    public void exportInpatientAdviceExcel(@Valid InpatientAdviceExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<InpatientAdviceDO> list = inpatientAdviceService.getInpatientAdviceList(exportReqVO);
        // 导出 Excel
        List<InpatientAdviceExcelVO> datas = InpatientAdviceConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "住院药学不合理用药.xls", "数据", InpatientAdviceExcelVO.class, datas);
    }

}
