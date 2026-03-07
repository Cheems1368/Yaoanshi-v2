package com.sqlapk.medicine.module.patient.api;

import com.sqlapk.medicine.module.patient.api.dto.prescription.ElectronicPrescriptionReqDTO;
import com.sqlapk.medicine.module.patient.api.dto.prescription.ElectronicPrescriptionRespDTO;

import java.util.List;

/**
 * 电子处方 API 接口
 *
 * @author sqlapk
 */
public interface ElectronicPrescriptionApi {

    /**
     * 创建电子处方
     *
     * @param reqDTO 处方请求
     * @return 处方ID
     */
    Long createPrescription(ElectronicPrescriptionReqDTO reqDTO);

    /**
     * 根据处方编号获取处方
     *
     * @param prescriptionCode 处方编号
     * @return 处方信息
     */
    ElectronicPrescriptionRespDTO getPrescriptionByCode(String prescriptionCode);

    /**
     * 根据工单ID获取处方
     *
     * @param orderId 工单ID
     * @return 处方信息
     */
    ElectronicPrescriptionRespDTO getPrescriptionByOrderId(Long orderId);

    /**
     * 获取患者处方列表
     *
     * @param patientId 患者ID
     * @param limit 限制数量
     * @return 处方列表
     */
    List<ElectronicPrescriptionRespDTO> getPatientPrescriptions(Long patientId, Integer limit);

    /**
     * 医生确认处方
     *
     * @param prescriptionId 处方ID
     * @param doctorId 医生ID
     * @param comments 审核意见
     * @return 是否成功
     */
    Boolean confirmPrescription(Long prescriptionId, Long doctorId, String comments);

    /**
     * 电子签名处方
     *
     * @param prescriptionId 处方ID
     * @param doctorId 医生ID
     * @param signature 电子签名
     * @param certificate 签名证书
     * @return 是否成功
     */
    Boolean signPrescription(Long prescriptionId, Long doctorId, String signature, String certificate);

    /**
     * 生成处方PDF
     *
     * @param prescriptionId 处方ID
     * @return PDF文件URL
     */
    String generatePrescriptionPDF(Long prescriptionId);

    /**
     * 生成处方编号
     *
     * @return 处方编号
     */
    String generatePrescriptionCode();

    /**
     * 获取医生待审核处方列表
     *
     * @param doctorId 医生ID
     * @return 处方列表
     */
    List<ElectronicPrescriptionRespDTO> getPendingPrescriptions(Long doctorId);

    /**
     * 更新配药状态
     *
     * @param prescriptionId 处方ID
     * @param status 配药状态
     * @return 是否成功
     */
    Boolean updateDispensingStatus(Long prescriptionId, String status);
}
