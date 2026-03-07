package com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.medicinereconciliationdetail.MedicineReconciliationDetailConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicinereconciliationdetail.MedicineReconciliationDetailDO;
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
@RequestMapping("/patient/medicine-reconciliation-detail")
@Validated
public class MedicineReconciliationDetailController {

    @Resource
    private MedicineReconciliationDetailService medicineReconciliationDetailService;

    @PostMapping("/create")
    @Operation(summary = "创建药物重整剂量")
    @PreAuthorize("@ss.hasPermission('patient:medicine-reconciliation-detail:create')")
    public CommonResult<Long> createMedicineReconciliationDetail(@Valid @RequestBody MedicineReconciliationDetailCreateReqVO createReqVO) {
        return success(medicineReconciliationDetailService.createMedicineReconciliationDetail(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新药物重整剂量")
    @PreAuthorize("@ss.hasPermission('patient:medicine-reconciliation-detail:update')")
    public CommonResult<Boolean> updateMedicineReconciliationDetail(@Valid @RequestBody MedicineReconciliationDetailUpdateReqVO updateReqVO) {
        medicineReconciliationDetailService.updateMedicineReconciliationDetail(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除药物重整剂量")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:medicine-reconciliation-detail:delete')")
    public CommonResult<Boolean> deleteMedicineReconciliationDetail(@RequestParam("id") Long id) {
        medicineReconciliationDetailService.deleteMedicineReconciliationDetail(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得药物重整剂量")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:medicine-reconciliation-detail:query')")
    public CommonResult<MedicineReconciliationDetailRespVO> getMedicineReconciliationDetail(@RequestParam("id") Long id) {
        MedicineReconciliationDetailDO medicineReconciliationDetail = medicineReconciliationDetailService.getMedicineReconciliationDetail(id);
        return success(MedicineReconciliationDetailConvert.INSTANCE.convert(medicineReconciliationDetail));
    }

    @GetMapping("/list")
    @Operation(summary = "获得药物重整剂量列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:medicine-reconciliation-detail:query')")
    public CommonResult<List<MedicineReconciliationDetailRespVO>> getMedicineReconciliationDetailList(@RequestParam("ids") Collection<Long> ids) {
        List<MedicineReconciliationDetailDO> list = medicineReconciliationDetailService.getMedicineReconciliationDetailList(ids);
        return success(MedicineReconciliationDetailConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得药物重整剂量分页")
    @PreAuthorize("@ss.hasPermission('patient:medicine-reconciliation-detail:query')")
    public CommonResult<PageResult<MedicineReconciliationDetailRespVO>> getMedicineReconciliationDetailPage(@Valid MedicineReconciliationDetailPageReqVO pageVO) {
        PageResult<MedicineReconciliationDetailDO> pageResult = medicineReconciliationDetailService.getMedicineReconciliationDetailPage(pageVO);
        return success(MedicineReconciliationDetailConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出药物重整剂量 Excel")
    @PreAuthorize("@ss.hasPermission('patient:medicine-reconciliation-detail:export')")
    @OperateLog(type = EXPORT)
    public void exportMedicineReconciliationDetailExcel(@Valid MedicineReconciliationDetailExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<MedicineReconciliationDetailDO> list = medicineReconciliationDetailService.getMedicineReconciliationDetailList(exportReqVO);
        // 导出 Excel
        List<MedicineReconciliationDetailExcelVO> datas = MedicineReconciliationDetailConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "药物重整剂量.xls", "数据", MedicineReconciliationDetailExcelVO.class, datas);
    }

}
