package com.sqlapk.medicine.module.patient.controller.app.prescription;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.security.core.annotations.PreAuthenticated;
import com.sqlapk.medicine.module.patient.api.ElectronicPrescriptionApi;
import com.sqlapk.medicine.module.patient.api.dto.prescription.ElectronicPrescriptionReqDTO;
import com.sqlapk.medicine.module.patient.api.dto.prescription.ElectronicPrescriptionRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;

/**
 * 电子处方控制器
 *
 * @author sqlapk
 */
@Tag(name = "用户 App - 电子处方")
@RestController
@RequestMapping("/app/prescription")
@Validated
@Slf4j
public class ElectronicPrescriptionController {

    @Autowired
    private ElectronicPrescriptionApi electronicPrescriptionApi;

    @PostMapping("/create")
    @Operation(summary = "创建电子处方")
    @PreAuthenticated
    public CommonResult<Long> createPrescription(@Valid @RequestBody ElectronicPrescriptionReqDTO reqDTO) {
        return success(electronicPrescriptionApi.createPrescription(reqDTO));
    }

    @GetMapping("/code/{prescriptionCode}")
    @Operation(summary = "根据处方编号获取处方")
    @PreAuthenticated
    public CommonResult<ElectronicPrescriptionRespDTO> getPrescriptionByCode(
            @Parameter(description = "处方编号", required = true) @PathVariable("prescriptionCode") String prescriptionCode) {
        return success(electronicPrescriptionApi.getPrescriptionByCode(prescriptionCode));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "根据工单ID获取处方")
    @PreAuthenticated
    public CommonResult<ElectronicPrescriptionRespDTO> getPrescriptionByOrderId(
            @Parameter(description = "工单ID", required = true) @PathVariable("orderId") Long orderId) {
        return success(electronicPrescriptionApi.getPrescriptionByOrderId(orderId));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "获取患者处方列表")
    @PreAuthenticated
    public CommonResult<List<ElectronicPrescriptionRespDTO>> getPatientPrescriptions(
            @Parameter(description = "患者ID", required = true) @PathVariable("patientId") Long patientId,
            @Parameter(description = "限制数量", example = "10") @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return success(electronicPrescriptionApi.getPatientPrescriptions(patientId, limit));
    }

    @PostMapping("/confirm/{prescriptionId}")
    @Operation(summary = "医生确认处方")
    @PreAuthenticated
    public CommonResult<Boolean> confirmPrescription(
            @Parameter(description = "处方ID", required = true) @PathVariable("prescriptionId") Long prescriptionId,
            @Parameter(description = "医生ID", required = true) @RequestParam("doctorId") Long doctorId,
            @Parameter(description = "审核意见") @RequestParam(value = "comments", defaultValue = "") String comments) {
        return success(electronicPrescriptionApi.confirmPrescription(prescriptionId, doctorId, comments));
    }

    @PostMapping("/sign/{prescriptionId}")
    @Operation(summary = "电子签名处方")
    @PreAuthenticated
    public CommonResult<Boolean> signPrescription(
            @Parameter(description = "处方ID", required = true) @PathVariable("prescriptionId") Long prescriptionId,
            @Parameter(description = "医生ID", required = true) @RequestParam("doctorId") Long doctorId,
            @Parameter(description = "电子签名", required = true) @RequestParam("signature") String signature,
            @Parameter(description = "签名证书") @RequestParam(value = "certificate", defaultValue = "") String certificate) {
        return success(electronicPrescriptionApi.signPrescription(prescriptionId, doctorId, signature, certificate));
    }

    @PostMapping("/generate-pdf/{prescriptionId}")
    @Operation(summary = "生成处方PDF")
    @PreAuthenticated
    public CommonResult<String> generatePrescriptionPDF(
            @Parameter(description = "处方ID", required = true) @PathVariable("prescriptionId") Long prescriptionId) {
        return success(electronicPrescriptionApi.generatePrescriptionPDF(prescriptionId));
    }

    @GetMapping("/generate-code")
    @Operation(summary = "生成处方编号")
    @PreAuthenticated
    public CommonResult<String> generatePrescriptionCode() {
        return success(electronicPrescriptionApi.generatePrescriptionCode());
    }

    @GetMapping("/pending/{doctorId}")
    @Operation(summary = "获取医生待审核处方列表")
    @PreAuthenticated
    public CommonResult<List<ElectronicPrescriptionRespDTO>> getPendingPrescriptions(
            @Parameter(description = "医生ID", required = true) @PathVariable("doctorId") Long doctorId) {
        return success(electronicPrescriptionApi.getPendingPrescriptions(doctorId));
    }

    @PostMapping("/dispensing-status/{prescriptionId}")
    @Operation(summary = "更新配药状态")
    @PreAuthenticated
    public CommonResult<Boolean> updateDispensingStatus(
            @Parameter(description = "处方ID", required = true) @PathVariable("prescriptionId") Long prescriptionId,
            @Parameter(description = "配药状态", required = true) @RequestParam("status") String status) {
        return success(electronicPrescriptionApi.updateDispensingStatus(prescriptionId, status));
    }
}
