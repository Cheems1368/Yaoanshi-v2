package com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationUpdateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailExportReqVO;
import com.sqlapk.medicine.module.patient.convert.medicinereconciliation.MedicineReconciliationConvert;
import com.sqlapk.medicine.module.patient.convert.medicinereconciliationdetail.MedicineReconciliationDetailConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicinereconciliation.MedicineReconciliationDO;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicinereconciliationdetail.MedicineReconciliationDetailDO;
import com.sqlapk.medicine.module.patient.service.medicinereconciliation.MedicineReconciliationService;
import com.sqlapk.medicine.module.patient.service.medicinereconciliationdetail.MedicineReconciliationDetailService;
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

@Tag(name = "管理后台 - 药物重整剂量")
@RestController
@RequestMapping("/patient/medicine-reconciliation")
@Validated
public class MedicineReconciliationController {

    @Resource
    private MedicineReconciliationService medicineReconciliationService;
    @Resource
    private MedicineReconciliationDetailService medicineReconciliationDetailService;

    @PostMapping("/create")
    @Operation(summary = "创建药物重整剂量")
    @PreAuthorize("@ss.hasPermission('patient:medicine-reconciliation:create')")
    public CommonResult<Long> createMedicineReconciliation(@Valid @RequestBody MedicineReconciliationCreateReqVO createReqVO) {
        return success(medicineReconciliationService.createMedicineReconciliation(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新药物重整剂量")
    @PreAuthorize("@ss.hasPermission('patient:medicine-reconciliation:update')")
    public CommonResult<Boolean> updateMedicineReconciliation(@Valid @RequestBody MedicineReconciliationUpdateReqVO updateReqVO) {
        medicineReconciliationService.updateMedicineReconciliation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除药物重整剂量")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:medicine-reconciliation:delete')")
    public CommonResult<Boolean> deleteMedicineReconciliation(@RequestParam("id") Long id) {
        medicineReconciliationService.deleteMedicineReconciliation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得药物重整剂量")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:medicine-reconciliation:query')")
    public CommonResult<MedicineReconciliationRespVO> getMedicineReconciliation(@RequestParam("id") Long id) {
        MedicineReconciliationDO medicineReconciliation = medicineReconciliationService.getMedicineReconciliation(id);
        MedicineReconciliationRespVO result = MedicineReconciliationConvert.INSTANCE.convert(medicineReconciliation);
        MedicineReconciliationDetailExportReqVO medicineReconciliationDetailExportReqVO = new MedicineReconciliationDetailExportReqVO();
        medicineReconciliationDetailExportReqVO.setReconciliationId(id);
        List<MedicineReconciliationDetailDO> medicineReconciliationDetailList = medicineReconciliationDetailService.getMedicineReconciliationDetailList(medicineReconciliationDetailExportReqVO);
        result.setDetails(MedicineReconciliationDetailConvert.INSTANCE.convertList(medicineReconciliationDetailList));
        return success(result);
    }

    @GetMapping("/list")
    @Operation(summary = "获得药物重整剂量列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:medicine-reconciliation:query')")
    public CommonResult<List<MedicineReconciliationRespVO>> getMedicineReconciliationList(@RequestParam("ids") Collection<Long> ids) {
        List<MedicineReconciliationDO> list = medicineReconciliationService.getMedicineReconciliationList(ids);
        return success(MedicineReconciliationConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得药物重整剂量分页")
    @PreAuthorize("@ss.hasPermission('patient:medicine-reconciliation:query')")
    public CommonResult<PageResult<MedicineReconciliationRespVO>> getMedicineReconciliationPage(@Valid MedicineReconciliationPageReqVO pageVO) {
        PageResult<MedicineReconciliationDO> pageResult = medicineReconciliationService.getMedicineReconciliationPage(pageVO);
        return success(MedicineReconciliationConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出药物重整剂量 Excel")
    @PreAuthorize("@ss.hasPermission('patient:medicine-reconciliation:export')")
    @OperateLog(type = EXPORT)
    public void exportMedicineReconciliationExcel(@Valid MedicineReconciliationExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<MedicineReconciliationDO> list = medicineReconciliationService.getMedicineReconciliationList(exportReqVO);
        // 导出 Excel
        List<MedicineReconciliationExcelVO> datas = MedicineReconciliationConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "药物重整剂量.xls", "数据", MedicineReconciliationExcelVO.class, datas);
    }

}
