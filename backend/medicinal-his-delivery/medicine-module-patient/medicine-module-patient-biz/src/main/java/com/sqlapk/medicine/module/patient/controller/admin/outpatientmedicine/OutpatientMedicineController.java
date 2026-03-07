package com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicinePageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.outpatientmedicine.OutpatientMedicineConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientmedicine.OutpatientMedicineDO;
import com.sqlapk.medicine.module.patient.service.outpatientmedicine.OutpatientMedicineService;
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

@Tag(name = "管理后台 - 药学门诊")
@RestController
@RequestMapping("/patient/outpatient-medicine")
@Validated
public class OutpatientMedicineController {

    @Resource
    private OutpatientMedicineService outpatientMedicineService;

    @PostMapping("/create")
    @Operation(summary = "创建药学门诊")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-medicine:create')")
    public CommonResult<Long> createOutpatientMedicine(@Valid @RequestBody OutpatientMedicineCreateReqVO createReqVO) {
        return success(outpatientMedicineService.createOutpatientMedicine(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新药学门诊")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-medicine:update')")
    public CommonResult<Boolean> updateOutpatientMedicine(@Valid @RequestBody OutpatientMedicineUpdateReqVO updateReqVO) {
        outpatientMedicineService.updateOutpatientMedicine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除药学门诊")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:outpatient-medicine:delete')")
    public CommonResult<Boolean> deleteOutpatientMedicine(@RequestParam("id") Long id) {
        outpatientMedicineService.deleteOutpatientMedicine(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得药学门诊")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-medicine:query')")
    public CommonResult<OutpatientMedicineRespVO> getOutpatientMedicine(@RequestParam("id") Long id) {
        OutpatientMedicineDO outpatientMedicine = outpatientMedicineService.getOutpatientMedicine(id);
        return success(OutpatientMedicineConvert.INSTANCE.convert(outpatientMedicine));
    }

    @GetMapping("/list")
    @Operation(summary = "获得药学门诊列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-medicine:query')")
    public CommonResult<List<OutpatientMedicineRespVO>> getOutpatientMedicineList(@RequestParam("ids") Collection<Long> ids) {
        List<OutpatientMedicineDO> list = outpatientMedicineService.getOutpatientMedicineList(ids);
        return success(OutpatientMedicineConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得药学门诊分页")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-medicine:query')")
    public CommonResult<PageResult<OutpatientMedicineRespVO>> getOutpatientMedicinePage(@Valid OutpatientMedicinePageReqVO pageVO) {
        PageResult<OutpatientMedicineDO> pageResult = outpatientMedicineService.getOutpatientMedicinePage(pageVO);
        return success(OutpatientMedicineConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出药学门诊 Excel")
    @PreAuthorize("@ss.hasPermission('patient:outpatient-medicine:export')")
    @OperateLog(type = EXPORT)
    public void exportOutpatientMedicineExcel(@Valid OutpatientMedicineExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<OutpatientMedicineDO> list = outpatientMedicineService.getOutpatientMedicineList(exportReqVO);
        // 导出 Excel
        List<OutpatientMedicineExcelVO> datas = OutpatientMedicineConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "药学门诊.xls", "数据", OutpatientMedicineExcelVO.class, datas);
    }

}
