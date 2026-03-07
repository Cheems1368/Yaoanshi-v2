package com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.excel.core.util.ExcelUtils;
import com.sqlapk.medicine.framework.operatelog.core.annotations.OperateLog;
import com.sqlapk.medicine.module.drugs.dal.dataobject.drug.DrugsDO;
import com.sqlapk.medicine.module.drugs.service.drug.DrugsService;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationExcelVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationUpdateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsSimpleRespVO;
import com.sqlapk.medicine.module.patient.convert.medicineconsultation.MedicineConsultationConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicineconsultation.MedicineConsultationDO;
import com.sqlapk.medicine.module.patient.service.medicineconsultation.MedicineConsultationService;
import com.sqlapk.medicine.module.patient.service.patient.PatientsService;
import com.sqlapk.medicine.module.system.api.user.AdminUserApi;
import com.sqlapk.medicine.module.system.api.user.dto.AdminUserRespDTO;
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
import java.util.Optional;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;
import static com.sqlapk.medicine.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 用药咨询")
@RestController
@RequestMapping("/patient/medicine-consultation")
@Validated
public class MedicineConsultationController {

    @Resource
    private MedicineConsultationService medicineConsultationService;
    @Resource
    private DrugsService drugsService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private PatientsService patientsService;

    @PostMapping("/create")
    @Operation(summary = "创建用药咨询")
    @PreAuthorize("@ss.hasPermission('patient:medicine-consultation:create')")
    public CommonResult<Long> createMedicineConsultation(@Valid @RequestBody MedicineConsultationCreateReqVO createReqVO) {
        return success(medicineConsultationService.createMedicineConsultation(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用药咨询")
    @PreAuthorize("@ss.hasPermission('patient:medicine-consultation:update')")
    public CommonResult<Boolean> updateMedicineConsultation(@Valid @RequestBody MedicineConsultationUpdateReqVO updateReqVO) {
        medicineConsultationService.updateMedicineConsultation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用药咨询")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('patient:medicine-consultation:delete')")
    public CommonResult<Boolean> deleteMedicineConsultation(@RequestParam("id") Long id) {
        medicineConsultationService.deleteMedicineConsultation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用药咨询")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('patient:medicine-consultation:query')")
    public CommonResult<MedicineConsultationRespVO> getMedicineConsultation(@RequestParam("id") Long id) {
        MedicineConsultationDO medicineConsultation = medicineConsultationService.getMedicineConsultation(id);
        MedicineConsultationRespVO result  = MedicineConsultationConvert.INSTANCE.convert(medicineConsultation);
        DrugsDO drugsDO = drugsService.get(result.getMedicineId());
        result.setMedicineName(drugsDO.getDrugName());
        AdminUserRespDTO user = adminUserApi.getUser(Long.valueOf(result.getDoctorId()));
        result.setDoctorName(user.getNickname());
        List<PatientsSimpleRespVO> patientSimple = patientsService.getPatientSimple();
        Optional<PatientsSimpleRespVO> first = patientSimple.stream()
                .filter(patient -> result.getPatientId().equals(patient.getId())).findFirst();
        result.setPatientName(first.isPresent()? first.get().getNickname() : "");
        return success(result);
    }

    @GetMapping("/list")
    @Operation(summary = "获得用药咨询列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('patient:medicine-consultation:query')")
    public CommonResult<List<MedicineConsultationRespVO>> getMedicineConsultationList(@RequestParam("ids") Collection<Long> ids) {
        List<MedicineConsultationDO> list = medicineConsultationService.getMedicineConsultationList(ids);
        return success(MedicineConsultationConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用药咨询分页")
    @PreAuthorize("@ss.hasPermission('patient:medicine-consultation:query')")
    public CommonResult<PageResult<MedicineConsultationRespVO>> getMedicineConsultationPage(@Valid MedicineConsultationPageReqVO pageVO) {
        PageResult<MedicineConsultationDO> pageResult = medicineConsultationService.getMedicineConsultationPage(pageVO);
        PageResult<MedicineConsultationRespVO> result = MedicineConsultationConvert.INSTANCE.convertPage(pageResult);
        result.getList().forEach(item -> {
            DrugsDO drugsDO = drugsService.get(item.getMedicineId());
            item.setMedicineName(drugsDO.getDrugName());
            AdminUserRespDTO user = adminUserApi.getUser(Long.valueOf(item.getDoctorId()));
            item.setDoctorName(user.getNickname());
            List<PatientsSimpleRespVO> patientSimple = patientsService.getPatientSimple();
            Optional<PatientsSimpleRespVO> first = patientSimple.stream()
                    .filter(patient -> item.getPatientId().equals(patient.getId())).findFirst();
            item.setPatientName(first.isPresent()? first.get().getNickname() : "");
        });
        return success(result);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出用药咨询 Excel")
    @PreAuthorize("@ss.hasPermission('patient:medicine-consultation:export')")
    @OperateLog(type = EXPORT)
    public void exportMedicineConsultationExcel(@Valid MedicineConsultationExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<MedicineConsultationDO> list = medicineConsultationService.getMedicineConsultationList(exportReqVO);
        // 导出 Excel
        List<MedicineConsultationExcelVO> datas = MedicineConsultationConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "用药咨询.xls", "数据", MedicineConsultationExcelVO.class, datas);
    }

}
