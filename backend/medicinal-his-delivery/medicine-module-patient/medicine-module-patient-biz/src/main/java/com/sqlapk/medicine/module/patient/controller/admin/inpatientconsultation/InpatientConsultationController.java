package com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.inpatientconsultation.InpatientConsultationConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientconsultation.InpatientConsultationDO;
import com.sqlapk.medicine.module.patient.service.inpatientconsultation.InpatientConsultationService;
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

@Tag(name = "管理后台 - 主要用药咨询")
@RestController
@RequestMapping("/patient/inpatient-consultation")
@Validated
public class InpatientConsultationController {

    @Resource
    private InpatientConsultationService inpatientConsultationService;

    @PostMapping("/create")
    @Operation(summary = "创建主要用药咨询")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-consultation:create')")
    public CommonResult<Long> createInpatientConsultation(@Valid @RequestBody InpatientConsultationCreateReqVO createReqVO) {
        return success(inpatientConsultationService.createInpatientConsultation(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新主要用药咨询")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-consultation:update')")
    public CommonResult<Boolean> updateInpatientConsultation(@Valid @RequestBody InpatientConsultationUpdateReqVO updateReqVO) {
        inpatientConsultationService.updateInpatientConsultation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除主要用药咨询")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:inpatient-consultation:delete')")
    public CommonResult<Boolean> deleteInpatientConsultation(@RequestParam("id") Long id) {
        inpatientConsultationService.deleteInpatientConsultation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得主要用药咨询")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-consultation:query')")
    public CommonResult<InpatientConsultationRespVO> getInpatientConsultation(@RequestParam("id") Long id) {
        InpatientConsultationDO inpatientConsultation = inpatientConsultationService.getInpatientConsultation(id);
        return success(InpatientConsultationConvert.INSTANCE.convert(inpatientConsultation));
    }

    @GetMapping("/list")
    @Operation(summary = "获得主要用药咨询列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-consultation:query')")
    public CommonResult<List<InpatientConsultationRespVO>> getInpatientConsultationList(@RequestParam("ids") Collection<Long> ids) {
        List<InpatientConsultationDO> list = inpatientConsultationService.getInpatientConsultationList(ids);
        return success(InpatientConsultationConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得主要用药咨询分页")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-consultation:query')")
    public CommonResult<PageResult<InpatientConsultationRespVO>> getInpatientConsultationPage(@Valid InpatientConsultationPageReqVO pageVO) {
        PageResult<InpatientConsultationDO> pageResult = inpatientConsultationService.getInpatientConsultationPage(pageVO);
        return success(InpatientConsultationConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出主要用药咨询 Excel")
    @PreAuthorize("@ss.hasPermission('patient:inpatient-consultation:export')")
    @OperateLog(type = EXPORT)
    public void exportInpatientConsultationExcel(@Valid InpatientConsultationExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<InpatientConsultationDO> list = inpatientConsultationService.getInpatientConsultationList(exportReqVO);
        // 导出 Excel
        List<InpatientConsultationExcelVO> datas = InpatientConsultationConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "主要用药咨询.xls", "数据", InpatientConsultationExcelVO.class, datas);
    }

}
