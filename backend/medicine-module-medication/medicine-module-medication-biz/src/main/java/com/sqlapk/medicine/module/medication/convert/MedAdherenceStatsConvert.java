package com.sqlapk.medicine.module.medication.convert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlapk.medicine.module.medication.api.dto.AdherenceStatsRespDTO;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedAdherenceStatsDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用药依从统计转换器
 *
 * @author sqlapk
 */
@Component
@Slf4j
public class MedAdherenceStatsConvert {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * DO转换为响应DTO
     */
    public AdherenceStatsRespDTO convert(MedAdherenceStatsDO source) {
        if (source == null) {
            return null;
        }
        
        AdherenceStatsRespDTO target = new AdherenceStatsRespDTO();
        target.setId(source.getId());
        target.setPatientId(source.getPatientId());
        target.setPeriod(source.getPeriod());
        target.setStartDate(source.getStartDate());
        target.setEndDate(source.getEndDate());
        target.setTotalTasks(source.getTotalTasks());
        target.setCompletedTasks(source.getCompletedTasks());
        target.setMissedTasks(source.getMissedTasks());
        target.setDelayedTasks(source.getDelayedTasks());
        target.setAdherenceRate(source.getAdherenceRate());
        target.setStatisticsDate(source.getStatisticsDate());
        target.setVersion(source.getVersion());
        target.setCreateTime(source.getCreateTime());
        target.setUpdateTime(source.getUpdateTime());
        
        // 忽略的字段：topMedications, periodName, adherenceRatePercent
        return target;
    }

    /**
     * 批量转换
     */
    public List<AdherenceStatsRespDTO> convert(List<MedAdherenceStatsDO> sources) {
        if (sources == null) {
            return null;
        }
        return sources.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    /**
     * 将JSON字符串转换为药品统计列表
     */
    public List<AdherenceStatsRespDTO.DrugStatsDTO> mapTopMedications(String topMedicationsJson) {
        if (topMedicationsJson == null || topMedicationsJson.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(topMedicationsJson, new TypeReference<List<AdherenceStatsRespDTO.DrugStatsDTO>>() {});
        } catch (Exception e) {
            log.error("解析常用药品JSON失败: {}", topMedicationsJson, e);
            return new ArrayList<>();
        }
    }

}
