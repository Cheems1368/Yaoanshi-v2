package com.sqlapk.medicine.module.patient.service.prescription;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil;
import com.sqlapk.medicine.module.patient.api.ElectronicPrescriptionApi;
import com.sqlapk.medicine.module.patient.api.dto.prescription.ElectronicPrescriptionReqDTO;
import com.sqlapk.medicine.module.patient.api.dto.prescription.ElectronicPrescriptionRespDTO;
import com.sqlapk.medicine.module.patient.convert.prescription.ElectronicPrescriptionConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescription.ElectronicPrescriptionDO;
import com.sqlapk.medicine.module.patient.dal.mysql.prescription.ElectronicPrescriptionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 电子处方服务实现
 *
 * @author sqlapk
 */
@Service
@Slf4j
public class ElectronicPrescriptionServiceImpl implements ElectronicPrescriptionApi {

    @Autowired
    private ElectronicPrescriptionMapper prescriptionMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public Long createPrescription(ElectronicPrescriptionReqDTO reqDTO) {
        log.info("创建电子处方，工单ID: {}, 患者ID: {}, 医生ID: {}", 
                reqDTO.getOrderId(), reqDTO.getPatientId(), reqDTO.getDoctorId());

        // 1. 数据验证
        validatePrescriptionData(reqDTO);

        // 2. 转换为DO对象
        ElectronicPrescriptionDO prescriptionDO = ElectronicPrescriptionConvert.INSTANCE.convert(reqDTO);
        
        // 3. 生成处方编号
        String prescriptionCode = generatePrescriptionCode();
        prescriptionDO.setPrescriptionCode(prescriptionCode);
        
        // 4. 设置默认值
        prescriptionDO.setStatus("draft");
        if (prescriptionDO.getValidityDays() == null) {
            prescriptionDO.setValidityDays(7); // 默认7天有效期
        }
        prescriptionDO.setExpiryTime(LocalDateTime.now().plusDays(prescriptionDO.getValidityDays()));

        // 5. 处理药品信息
        try {
            String medicationsJson = objectMapper.writeValueAsString(reqDTO.getMedications());
            prescriptionDO.setMedications(medicationsJson);
        } catch (Exception e) {
            log.error("处理药品信息JSON失败", e);
            throw ServiceExceptionUtil.exception(500, "药品信息处理失败");
        }

        // 6. 保存处方
        prescriptionMapper.insert(prescriptionDO);

        log.info("电子处方创建成功，处方ID: {}, 处方编号: {}", prescriptionDO.getId(), prescriptionCode);
        return prescriptionDO.getId();
    }

    @Override
    public ElectronicPrescriptionRespDTO getPrescriptionByCode(String prescriptionCode) {
        log.info("根据处方编号获取处方，处方编号: {}", prescriptionCode);

        ElectronicPrescriptionDO prescriptionDO = prescriptionMapper.selectByPrescriptionCode(prescriptionCode);
        if (prescriptionDO == null) {
            throw ServiceExceptionUtil.exception(404, "处方不存在");
        }

        return convertToRespDTO(prescriptionDO);
    }

    @Override
    public ElectronicPrescriptionRespDTO getPrescriptionByOrderId(Long orderId) {
        log.info("根据工单ID获取处方，工单ID: {}", orderId);

        List<ElectronicPrescriptionDO> prescriptions = prescriptionMapper.selectByOrderId(orderId);
        if (prescriptions.isEmpty()) {
            throw ServiceExceptionUtil.exception(404, "未找到相关处方");
        }

        // 返回最新的处方
        ElectronicPrescriptionDO latestPrescription = prescriptions.get(0);
        return convertToRespDTO(latestPrescription);
    }

    @Override
    public List<ElectronicPrescriptionRespDTO> getPatientPrescriptions(Long patientId, Integer limit) {
        log.info("获取患者处方列表，患者ID: {}, 限制数量: {}", patientId, limit);

        List<ElectronicPrescriptionDO> prescriptions = prescriptionMapper.selectByPatientId(patientId, limit);
        return prescriptions.stream()
                .map(this::convertToRespDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Boolean confirmPrescription(Long prescriptionId, Long doctorId, String comments) {
        log.info("医生确认处方，处方ID: {}, 医生ID: {}, 意见: {}", prescriptionId, doctorId, comments);

        ElectronicPrescriptionDO prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null) {
            throw ServiceExceptionUtil.exception(404, "处方不存在");
        }

        if (!prescription.getDoctorId().equals(doctorId)) {
            throw ServiceExceptionUtil.exception(403, "无权限确认此处方");
        }

        if (!"draft".equals(prescription.getStatus())) {
            throw ServiceExceptionUtil.exception(400, "处方状态不允许确认");
        }

        // 更新处方状态
        prescription.setStatus("confirmed");
        prescription.setReviewComments(comments);
        prescription.setReviewTime(LocalDateTime.now());
        prescriptionMapper.updateById(prescription);

        log.info("处方确认成功，处方ID: {}", prescriptionId);
        return true;
    }

    @Override
    @Transactional
    public Boolean signPrescription(Long prescriptionId, Long doctorId, String signature, String certificate) {
        log.info("电子签名处方，处方ID: {}, 医生ID: {}", prescriptionId, doctorId);

        ElectronicPrescriptionDO prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null) {
            throw ServiceExceptionUtil.exception(404, "处方不存在");
        }

        if (!prescription.getDoctorId().equals(doctorId)) {
            throw ServiceExceptionUtil.exception(403, "无权限签名此处方");
        }

        if (!"confirmed".equals(prescription.getStatus())) {
            throw ServiceExceptionUtil.exception(400, "处方状态不允许签名");
        }

        // 更新电子签名
        LocalDateTime signatureTime = LocalDateTime.now();
        int result = prescriptionMapper.updateDigitalSignature(prescriptionId, signature, signatureTime, certificate);
        
        if (result > 0) {
            log.info("处方电子签名成功，处方ID: {}", prescriptionId);
            return true;
        } else {
            log.error("处方电子签名失败，处方ID: {}", prescriptionId);
            return false;
        }
    }

    @Override
    public String generatePrescriptionPDF(Long prescriptionId) {
        log.info("生成处方PDF，处方ID: {}", prescriptionId);

        ElectronicPrescriptionDO prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null) {
            throw ServiceExceptionUtil.exception(404, "处方不存在");
        }

        if (!"signed".equals(prescription.getStatus())) {
            throw ServiceExceptionUtil.exception(400, "处方未签名，无法生成PDF");
        }

        try {
            // TODO: 集成PDF生成服务（如iText、Flying Saucer等）
            // 这里先返回模拟的PDF URL
            String pdfUrl = "/prescriptions/pdf/" + prescription.getPrescriptionCode() + ".pdf";
            
            // 更新PDF URL
            prescriptionMapper.updatePdfUrl(prescriptionId, pdfUrl);
            
            log.info("处方PDF生成成功，处方ID: {}, PDF URL: {}", prescriptionId, pdfUrl);
            return pdfUrl;
            
        } catch (Exception e) {
            log.error("生成处方PDF失败", e);
            throw ServiceExceptionUtil.exception(500, "PDF生成失败");
        }
    }

    @Override
    public String generatePrescriptionCode() {
        // 生成格式：RX-YYYYMMDD-XXXX
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = String.format("%04d", new Random().nextInt(10000));
        return "RX-" + dateStr + "-" + randomStr;
    }

    @Override
    public List<ElectronicPrescriptionRespDTO> getPendingPrescriptions(Long doctorId) {
        log.info("获取医生待审核处方列表，医生ID: {}", doctorId);

        List<ElectronicPrescriptionDO> prescriptions = prescriptionMapper.selectPendingByDoctorId(doctorId);
        return prescriptions.stream()
                .map(this::convertToRespDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Boolean updateDispensingStatus(Long prescriptionId, String status) {
        log.info("更新配药状态，处方ID: {}, 状态: {}", prescriptionId, status);

        ElectronicPrescriptionDO prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null) {
            throw ServiceExceptionUtil.exception(404, "处方不存在");
        }

        // 更新配药状态
        prescription.setDispensingStatus(status);
        if ("dispensed".equals(status)) {
            prescription.setDispensingTime(LocalDateTime.now());
        } else if ("picked_up".equals(status)) {
            prescription.setPickupTime(LocalDateTime.now());
        }
        
        prescriptionMapper.updateById(prescription);

        log.info("配药状态更新成功，处方ID: {}, 状态: {}", prescriptionId, status);
        return true;
    }

    /**
     * 验证处方数据
     */
    private void validatePrescriptionData(ElectronicPrescriptionReqDTO reqDTO) {
        if (reqDTO.getOrderId() == null) {
            throw ServiceExceptionUtil.exception(400, "工单ID不能为空");
        }
        
        if (reqDTO.getPatientId() == null) {
            throw ServiceExceptionUtil.exception(400, "患者ID不能为空");
        }
        
        if (reqDTO.getDoctorId() == null) {
            throw ServiceExceptionUtil.exception(400, "医生ID不能为空");
        }
        
        if (reqDTO.getDiagnosis() == null || reqDTO.getDiagnosis().trim().isEmpty()) {
            throw ServiceExceptionUtil.exception(400, "诊断信息不能为空");
        }
        
        if (reqDTO.getMedications() == null || reqDTO.getMedications().isEmpty()) {
            throw ServiceExceptionUtil.exception(400, "处方药品不能为空");
        }
    }

    /**
     * 转换为响应DTO
     */
    private ElectronicPrescriptionRespDTO convertToRespDTO(ElectronicPrescriptionDO prescriptionDO) {
        ElectronicPrescriptionRespDTO respDTO = ElectronicPrescriptionConvert.INSTANCE.convert(prescriptionDO);
        
        // 处理药品信息JSON
        if (prescriptionDO.getMedications() != null && !prescriptionDO.getMedications().isEmpty()) {
            try {
                List<ElectronicPrescriptionReqDTO.MedicationDTO> medications = 
                        objectMapper.readValue(prescriptionDO.getMedications(), 
                                new TypeReference<List<ElectronicPrescriptionReqDTO.MedicationDTO>>() {});
                
                // 转换为响应DTO中的药品信息
                List<ElectronicPrescriptionRespDTO.MedicationDTO> respMedications = medications.stream()
                        .map(med -> {
                            ElectronicPrescriptionRespDTO.MedicationDTO respMed = new ElectronicPrescriptionRespDTO.MedicationDTO();
                            respMed.setDrugName(med.getDrugName());
                            respMed.setSpecification(med.getSpecification());
                            respMed.setDosage(med.getDosage());
                            respMed.setUsage(med.getUsage());
                            respMed.setFrequency(med.getFrequency());
                            respMed.setQuantity(med.getQuantity());
                            respMed.setUnit(med.getUnit());
                            respMed.setNotes(med.getNotes());
                            return respMed;
                        })
                        .collect(Collectors.toList());
                
                respDTO.setMedications(respMedications);
            } catch (Exception e) {
                log.error("解析药品信息JSON失败", e);
            }
        }
        
        return respDTO;
    }
}
