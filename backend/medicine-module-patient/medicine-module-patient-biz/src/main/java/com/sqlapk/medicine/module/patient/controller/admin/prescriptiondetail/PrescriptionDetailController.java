package com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.drugs.dal.dataobject.drug.DrugsDO;
import com.sqlapk.medicine.module.drugs.service.drug.DrugsService;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.prescriptiondetail.PrescriptionDetailConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptiondetail.PrescriptionDetailDO;
import com.sqlapk.medicine.module.patient.service.prescriptiondetail.PrescriptionDetailService;
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

@Tag(name = "管理后台 - 患者处方")
@RestController
@RequestMapping("/patient/prescription-detail")
@Validated
public class PrescriptionDetailController {

    @Resource
    private PrescriptionDetailService prescriptionDetailService;
    @Resource
    private DrugsService drugsService;
    @Resource
    private PrescriptionDetailConvert prescriptionDetailConvert;

    @PostMapping("/create")
    @Operation(summary = "创建患者处方")
    @PreAuthorize("@ss.hasPermission('patient:prescription-detail:create')")
    public CommonResult<Long> createPrescriptionDetail(@Valid @RequestBody PrescriptionDetailCreateReqVO createReqVO) {
        return success(prescriptionDetailService.createPrescriptionDetail(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新患者处方")
    @PreAuthorize("@ss.hasPermission('patient:prescription-detail:update')")
    public CommonResult<Boolean> updatePrescriptionDetail(@Valid @RequestBody PrescriptionDetailUpdateReqVO updateReqVO) {
        prescriptionDetailService.updatePrescriptionDetail(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除患者处方")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:prescription-detail:delete')")
    public CommonResult<Boolean> deletePrescriptionDetail(@RequestParam("id") Long id) {
        prescriptionDetailService.deletePrescriptionDetail(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得患者处方")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:prescription-detail:query')")
    public CommonResult<PrescriptionDetailRespVO> getPrescriptionDetail(@RequestParam("id") Long id) {
        PrescriptionDetailDO prescriptionDetail = prescriptionDetailService.getPrescriptionDetail(id);
        return success(prescriptionDetailConvert.convert(prescriptionDetail));
    }

    @GetMapping("/list")
    @Operation(summary = "获得患者处方列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:prescription-detail:query')")
    public CommonResult<List<PrescriptionDetailRespVO>> getPrescriptionDetailList(@RequestParam("ids") Collection<Long> ids) {
        List<PrescriptionDetailDO> list = prescriptionDetailService.getPrescriptionDetailList(ids);
        return success(prescriptionDetailConvert.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得患者处方分页")
    @PreAuthorize("@ss.hasPermission('patient:prescription-detail:query')")
    public CommonResult<PageResult<PrescriptionDetailRespVO>> getPrescriptionDetailPage(@Valid PrescriptionDetailPageReqVO pageVO) {
        PageResult<PrescriptionDetailDO> pageResult = prescriptionDetailService.getPrescriptionDetailPage(pageVO);
        PageResult<PrescriptionDetailRespVO> result = prescriptionDetailConvert.convertPage(pageResult);
        result.getList().forEach(item -> {
            DrugsDO drugsDO = drugsService.get(item.getMedicineId());
            item.setName(drugsDO.getDrugName());
            item.setMedicineName(drugsDO.getDrugName());
            item.setDrugId(item.getId());
        });
        return success(result);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出患者处方 Excel")
    @PreAuthorize("@ss.hasPermission('patient:prescription-detail:export')")
    @OperateLog(type = EXPORT)
    public void exportPrescriptionDetailExcel(@Valid PrescriptionDetailExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<PrescriptionDetailDO> list = prescriptionDetailService.getPrescriptionDetailList(exportReqVO);
        // 导出 Excel
        List<PrescriptionDetailExcelVO> datas = prescriptionDetailConvert.convertList02(list);
        ExcelUtils.write(response, "患者处方.xls", "数据", PrescriptionDetailExcelVO.class, datas);
    }

}
