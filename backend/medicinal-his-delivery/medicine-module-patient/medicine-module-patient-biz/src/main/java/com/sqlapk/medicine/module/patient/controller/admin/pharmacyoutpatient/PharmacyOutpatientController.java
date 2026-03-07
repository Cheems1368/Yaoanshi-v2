package com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.pharmacyoutpatient.PharmacyOutpatientConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.pharmacyoutpatient.PharmacyOutpatientDO;
import com.sqlapk.medicine.module.patient.service.pharmacyoutpatient.PharmacyOutpatientService;
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
@RequestMapping("/patient/pharmacy-outpatient")
@Validated
public class PharmacyOutpatientController {

    @Resource
    private PharmacyOutpatientService pharmacyOutpatientService;

    @PostMapping("/create")
    @Operation(summary = "创建药学门诊")
    @PreAuthorize("@ss.hasPermission('patient:pharmacy-outpatient:create')")
    public CommonResult<Long> createPharmacyOutpatient(@Valid @RequestBody PharmacyOutpatientCreateReqVO createReqVO) {
        return success(pharmacyOutpatientService.createPharmacyOutpatient(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新药学门诊")
    @PreAuthorize("@ss.hasPermission('patient:pharmacy-outpatient:update')")
    public CommonResult<Boolean> updatePharmacyOutpatient(@Valid @RequestBody PharmacyOutpatientUpdateReqVO updateReqVO) {
        pharmacyOutpatientService.updatePharmacyOutpatient(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除药学门诊")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:pharmacy-outpatient:delete')")
    public CommonResult<Boolean> deletePharmacyOutpatient(@RequestParam("id") Long id) {
        pharmacyOutpatientService.deletePharmacyOutpatient(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得药学门诊")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:pharmacy-outpatient:query')")
    public CommonResult<PharmacyOutpatientRespVO> getPharmacyOutpatient(@RequestParam("id") Long id) {
        PharmacyOutpatientDO pharmacyOutpatient = pharmacyOutpatientService.getPharmacyOutpatient(id);
        return success(PharmacyOutpatientConvert.INSTANCE.convert(pharmacyOutpatient));
    }

    @GetMapping("/list")
    @Operation(summary = "获得药学门诊列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:pharmacy-outpatient:query')")
    public CommonResult<List<PharmacyOutpatientRespVO>> getPharmacyOutpatientList(@RequestParam("ids") Collection<Long> ids) {
        List<PharmacyOutpatientDO> list = pharmacyOutpatientService.getPharmacyOutpatientList(ids);
        return success(PharmacyOutpatientConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得药学门诊分页")
    @PreAuthorize("@ss.hasPermission('patient:pharmacy-outpatient:query')")
    public CommonResult<PageResult<PharmacyOutpatientRespVO>> getPharmacyOutpatientPage(@Valid PharmacyOutpatientPageReqVO pageVO) {
        PageResult<PharmacyOutpatientDO> pageResult = pharmacyOutpatientService.getPharmacyOutpatientPage(pageVO);
        return success(PharmacyOutpatientConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出药学门诊 Excel")
    @PreAuthorize("@ss.hasPermission('patient:pharmacy-outpatient:export')")
    @OperateLog(type = EXPORT)
    public void exportPharmacyOutpatientExcel(@Valid PharmacyOutpatientExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<PharmacyOutpatientDO> list = pharmacyOutpatientService.getPharmacyOutpatientList(exportReqVO);
        // 导出 Excel
        List<PharmacyOutpatientExcelVO> datas = PharmacyOutpatientConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "药学门诊.xls", "数据", PharmacyOutpatientExcelVO.class, datas);
    }

}
