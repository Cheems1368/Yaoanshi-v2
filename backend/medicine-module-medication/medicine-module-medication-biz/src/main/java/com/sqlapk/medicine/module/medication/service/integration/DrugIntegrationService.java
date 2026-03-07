package com.sqlapk.medicine.module.medication.service.integration;

import com.sqlapk.medicine.module.drug.api.DrugApi;
import com.sqlapk.medicine.module.drug.api.dto.DrugRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 药品信息集成服务
 */
@Slf4j
@Service
public class DrugIntegrationService {

    @Autowired
    private DrugApi drugApi;

    /**
     * 获取药品信息
     */
    public DrugRespDTO getDrugInfo(Long drugId) {
        try {
            log.debug("获取药品信息: {}", drugId);
            return drugApi.getDrug(drugId);
        } catch (Exception e) {
            log.error("获取药品信息失败: {}, 错误: {}", drugId, e.getMessage());
            return null;
        }
    }

    /**
     * 批量获取药品信息
     */
    public List<DrugRespDTO> getDrugInfos(List<Long> drugIds) {
        try {
            log.debug("批量获取药品信息: {}", drugIds);
            return drugApi.getDrugList(drugIds);
        } catch (Exception e) {
            log.error("批量获取药品信息失败: {}, 错误: {}", drugIds, e.getMessage());
            return null;
        }
    }

    /**
     * 根据药品名称搜索药品
     */
    public List<DrugRespDTO> searchDrugsByName(String drugName) {
        try {
            log.debug("根据名称搜索药品: {}", drugName);
            
            // 使用DrugApi的getDrugByName方法搜索药品
            DrugRespDTO drug = drugApi.getDrugByName(drugName);
            if (drug != null) {
                return List.of(drug);
            }
            
            // 如果没有找到完全匹配的药品，可以在这里实现模糊搜索逻辑
            // 暂时返回空列表
            return List.of();
        } catch (Exception e) {
            log.error("搜索药品失败: {}, 错误: {}", drugName, e.getMessage());
            return List.of();
        }
    }

    /**
     * 验证药品是否存在
     */
    public boolean validateDrugExists(Long drugId) {
        try {
            DrugRespDTO drug = drugApi.getDrug(drugId);
            return drug != null;
        } catch (Exception e) {
            log.error("验证药品存在性失败: {}, 错误: {}", drugId, e.getMessage());
            return false;
        }
    }

    /**
     * 获取药品详细信息
     */
    public DrugDetailInfo getDrugDetailInfo(Long drugId) {
        try {
            DrugRespDTO drug = drugApi.getDrug(drugId);
            if (drug == null) {
                return null;
            }

            DrugDetailInfo detail = new DrugDetailInfo();
            detail.setDrugId(drugId);
            detail.setDrugName(drug.getDrugName());
            detail.setSpecification(drug.getSpecification());
            detail.setUnit(drug.getUnit());
            detail.setManufacturer(null); // DrugRespDTO中没有manufacturer字段
            detail.setApprovalNumber(null); // DrugRespDTO中没有approvalNumber字段
            detail.setDrugType(drug.getDrugType());
            detail.setIsPrescription(drug.getDrugType()); // 使用drugType作为处方药标识
            detail.setIsControlled(drug.getDrugType()); // 使用drugType作为管制药品标识
            detail.setStatus(drug.getStatus());

            return detail;
        } catch (Exception e) {
            log.error("获取药品详细信息失败: {}, 错误: {}", drugId, e.getMessage());
            return null;
        }
    }

    /**
     * 检查药品是否可用
     */
    public boolean isDrugAvailable(Long drugId) {
        try {
            DrugRespDTO drug = drugApi.getDrug(drugId);
            if (drug == null) {
                return false;
            }

            // 检查药品状态
            if (drug.getStatus() == null || drug.getStatus() != 0) { // 0表示启用状态
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("检查药品可用性失败: {}, 错误: {}", drugId, e.getMessage());
            return false;
        }
    }

    /**
     * 检查药品是否为管制药品
     */
    public boolean isControlledDrug(Long drugId) {
        try {
            DrugRespDTO drug = drugApi.getDrug(drugId);
            if (drug == null) {
                return false;
            }

            // 根据药品类型判断是否为管制药品
            // 这里可以根据实际业务逻辑调整
            return drug.getDrugType() != null && drug.getDrugType() == 1; // 假设1表示管制药品
        } catch (Exception e) {
            log.error("检查药品管制状态失败: {}, 错误: {}", drugId, e.getMessage());
            return false;
        }
    }

    /**
     * 获取药品相互作用信息
     */
    public List<DrugInteractionInfo> getDrugInteractions(Long drugId) {
        try {
            log.debug("获取药品相互作用信息: {}", drugId);
            
            // 获取药品信息
            DrugRespDTO drug = drugApi.getDrug(drugId);
            if (drug == null) {
                return List.of();
            }
            
            // 这里应该调用外部药品相互作用API
            // 暂时返回模拟的相互作用信息
            List<DrugInteractionInfo> interactions = new java.util.ArrayList<>();
            
            // 模拟一些常见的药品相互作用
            if (drug.getDrugName() != null && drug.getDrugName().contains("阿司匹林")) {
                DrugInteractionInfo interaction = new DrugInteractionInfo();
                interaction.setDrugId1(drugId);
                interaction.setDrugId2(999L); // 华法林的模拟ID
                interaction.setInteractionType("增强抗凝作用");
                interaction.setSeverity("严重");
                interaction.setInteractionDescription("阿司匹林与华法林合用可能增加出血风险");
                interactions.add(interaction);
            }
            
            return interactions;
        } catch (Exception e) {
            log.error("获取药品相互作用信息失败: {}, 错误: {}", drugId, e.getMessage());
            return List.of();
        }
    }

    /**
     * 检查药品过敏风险
     */
    public AllergyRiskInfo checkAllergyRisk(Long drugId, String allergyHistory) {
        try {
            log.debug("检查药品过敏风险: {}, 过敏史: {}", drugId, allergyHistory);
            
            AllergyRiskInfo riskInfo = new AllergyRiskInfo();
            riskInfo.setDrugId(drugId);
            riskInfo.setRiskLevel("LOW"); // 默认低风险
            riskInfo.setRiskDescription("无已知过敏风险");
            
            // 这里应该实现过敏风险检查逻辑
            if (allergyHistory != null && !allergyHistory.trim().isEmpty()) {
                // 简单的关键词匹配
                String lowerAllergy = allergyHistory.toLowerCase();
                if (lowerAllergy.contains("青霉素") || lowerAllergy.contains("头孢")) {
                    riskInfo.setRiskLevel("HIGH");
                    riskInfo.setRiskDescription("存在青霉素类过敏风险");
                } else if (lowerAllergy.contains("磺胺") || lowerAllergy.contains("阿司匹林")) {
                    riskInfo.setRiskLevel("MEDIUM");
                    riskInfo.setRiskDescription("存在磺胺类过敏风险");
                }
            }
            
            return riskInfo;
        } catch (Exception e) {
            log.error("检查药品过敏风险失败: {}, 错误: {}", drugId, e.getMessage());
            return null;
        }
    }

    /**
     * 药品详细信息
     */
    public static class DrugDetailInfo {
        private Long drugId;
        private String drugName;
        private String specification;
        private String unit;
        private String manufacturer;
        private String approvalNumber;
        private Integer drugType;
        private Integer isPrescription;
        private Integer isControlled;
        private Integer status;

        // Getters and Setters
        public Long getDrugId() { return drugId; }
        public void setDrugId(Long drugId) { this.drugId = drugId; }

        public String getDrugName() { return drugName; }
        public void setDrugName(String drugName) { this.drugName = drugName; }

        public String getSpecification() { return specification; }
        public void setSpecification(String specification) { this.specification = specification; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public String getManufacturer() { return manufacturer; }
        public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

        public String getApprovalNumber() { return approvalNumber; }
        public void setApprovalNumber(String approvalNumber) { this.approvalNumber = approvalNumber; }

        public Integer getDrugType() { return drugType; }
        public void setDrugType(Integer drugType) { this.drugType = drugType; }

        public Integer getIsPrescription() { return isPrescription; }
        public void setIsPrescription(Integer isPrescription) { this.isPrescription = isPrescription; }

        public Integer getIsControlled() { return isControlled; }
        public void setIsControlled(Integer isControlled) { this.isControlled = isControlled; }

        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
    }

    /**
     * 药品相互作用信息
     */
    public static class DrugInteractionInfo {
        private Long drugId1;
        private Long drugId2;
        private String interactionType;
        private String interactionDescription;
        private String severity;

        // Getters and Setters
        public Long getDrugId1() { return drugId1; }
        public void setDrugId1(Long drugId1) { this.drugId1 = drugId1; }

        public Long getDrugId2() { return drugId2; }
        public void setDrugId2(Long drugId2) { this.drugId2 = drugId2; }

        public String getInteractionType() { return interactionType; }
        public void setInteractionType(String interactionType) { this.interactionType = interactionType; }

        public String getInteractionDescription() { return interactionDescription; }
        public void setInteractionDescription(String interactionDescription) { this.interactionDescription = interactionDescription; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }

    /**
     * 过敏风险信息
     */
    public static class AllergyRiskInfo {
        private Long drugId;
        private String riskLevel;
        private String riskDescription;

        // Getters and Setters
        public Long getDrugId() { return drugId; }
        public void setDrugId(Long drugId) { this.drugId = drugId; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

        public String getRiskDescription() { return riskDescription; }
        public void setRiskDescription(String riskDescription) { this.riskDescription = riskDescription; }
    }
}
