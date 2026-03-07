package com.sqlapk.medicine.module.medication.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlapk.medicine.framework.common.exception.ServiceException;
import com.sqlapk.medicine.module.medication.api.MedAdherenceStatsService;
import com.sqlapk.medicine.module.medication.api.dto.AdherenceStatsRespDTO;
import com.sqlapk.medicine.module.medication.convert.MedAdherenceStatsConvert;
import com.sqlapk.medicine.module.medication.dal.dataobject.MedAdherenceStatsDO;
import com.sqlapk.medicine.module.medication.dal.mysql.MedAdherenceStatsMapper;
import com.sqlapk.medicine.module.medication.enums.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 依从统计服务实现
 *
 * @author sqlapk
 */
@Service
@Slf4j
public class MedAdherenceStatsServiceImpl implements MedAdherenceStatsService {

    @Autowired
    private MedAdherenceStatsConvert medAdherenceStatsConvert;

    @Autowired
    private MedAdherenceStatsMapper medAdherenceStatsMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public Long calculateAndGenerateStats(Long patientId, Integer period, LocalDate startDate, LocalDate endDate) {
        log.info("计算并生成依从统计，患者ID: {}, 周期: {}, 日期范围: {} - {}", patientId, period, startDate, endDate);

        // 1. 计算统计数据
        AdherenceStatsRespDTO statsDTO = calculateStats(patientId, period, startDate, endDate);

        // 2. 检查是否已存在相同统计
        MedAdherenceStatsDO existingStats = medAdherenceStatsMapper.selectByPatientIdAndDateRange(
            patientId, startDate, endDate);
        
        if (existingStats != null) {
            // 更新现有统计
            existingStats.setTotalTasks(statsDTO.getTotalTasks());
            existingStats.setCompletedTasks(statsDTO.getCompletedTasks());
            existingStats.setMissedTasks(statsDTO.getMissedTasks());
            existingStats.setDelayedTasks(statsDTO.getDelayedTasks());
            existingStats.setAdherenceRate(statsDTO.getAdherenceRate());
            existingStats.setTopMedications(convertTopMedicationsToJson(statsDTO.getTopMedications()));
            existingStats.setStatisticsDate(LocalDate.now());
            existingStats.setVersion(existingStats.getVersion() + 1);
            
            medAdherenceStatsMapper.updateById(existingStats);
            log.info("依从统计更新成功，统计ID: {}", existingStats.getId());
            return existingStats.getId();
        } else {
            // 创建新统计
            MedAdherenceStatsDO statsDO = MedAdherenceStatsDO.builder()
                .patientId(patientId)
                .period(period)
                .startDate(startDate)
                .endDate(endDate)
                .totalTasks(statsDTO.getTotalTasks())
                .completedTasks(statsDTO.getCompletedTasks())
                .missedTasks(statsDTO.getMissedTasks())
                .delayedTasks(statsDTO.getDelayedTasks())
                .adherenceRate(statsDTO.getAdherenceRate())
                .topMedications(convertTopMedicationsToJson(statsDTO.getTopMedications()))
                .statisticsDate(LocalDate.now())
                .version(1)
                .build();

            medAdherenceStatsMapper.insert(statsDO);
            log.info("依从统计生成成功，统计ID: {}", statsDO.getId());
            return statsDO.getId();
        }
    }

    @Override
    public AdherenceStatsRespDTO getLatestStats(Long patientId, Integer period) {
        log.info("获取最新依从统计，患者ID: {}, 周期: {}", patientId, period);

        MedAdherenceStatsDO statsDO = medAdherenceStatsMapper.selectLatestByPatientIdAndPeriod(patientId, period);
        if (statsDO == null) {
            throw new ServiceException(ErrorCodeConstants.ADHERENCE_STATS_NOT_EXISTS);
        }

        AdherenceStatsRespDTO respDTO = medAdherenceStatsConvert.convert(statsDO);
        fillAdditionalFields(respDTO);

        return respDTO;
    }

    @Override
    public AdherenceStatsRespDTO getStatsByDateRange(Long patientId, LocalDate startDate, LocalDate endDate) {
        log.info("获取指定日期范围依从统计，患者ID: {}, 日期范围: {} - {}", patientId, startDate, endDate);

        MedAdherenceStatsDO statsDO = medAdherenceStatsMapper.selectByPatientIdAndDateRange(patientId, startDate, endDate);
        if (statsDO == null) {
            throw new ServiceException(ErrorCodeConstants.ADHERENCE_STATS_NOT_EXISTS);
        }

        AdherenceStatsRespDTO respDTO = medAdherenceStatsConvert.convert(statsDO);
        fillAdditionalFields(respDTO);

        return respDTO;
    }

    @Override
    public List<AdherenceStatsRespDTO> getHistoryStats(Long patientId, Integer period) {
        log.info("获取历史依从统计，患者ID: {}, 周期: {}", patientId, period);

        List<MedAdherenceStatsDO> statsDOs = medAdherenceStatsMapper.selectHistoryByPatientIdAndPeriod(patientId, period);
        List<AdherenceStatsRespDTO> respDTOs = medAdherenceStatsConvert.convert(statsDOs);

        for (AdherenceStatsRespDTO respDTO : respDTOs) {
            fillAdditionalFields(respDTO);
        }

        return respDTOs;
    }

    @Override
    @Transactional
    public void batchCalculateStats(List<Long> patientIds, Integer period) {
        log.info("批量计算依从统计，患者数量: {}, 周期: {}", patientIds.size(), period);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(period);

        for (Long patientId : patientIds) {
            try {
                calculateAndGenerateStats(patientId, period, startDate, endDate);
            } catch (Exception e) {
                log.error("计算患者 {} 的依从统计失败", patientId, e);
            }
        }

        log.info("批量计算依从统计完成");
    }

    @Override
    @Transactional
    public void recalculateStats(Long patientId, Integer period, LocalDate startDate, LocalDate endDate) {
        log.info("重新计算依从统计，患者ID: {}, 周期: {}, 日期范围: {} - {}", patientId, period, startDate, endDate);

        // 删除现有统计
        MedAdherenceStatsDO existingStats = medAdherenceStatsMapper.selectByPatientIdAndDateRange(patientId, startDate, endDate);
        if (existingStats != null) {
            medAdherenceStatsMapper.deleteById(existingStats.getId());
        }

        // 重新计算
        calculateAndGenerateStats(patientId, period, startDate, endDate);
    }

    @Override
    public AdherenceStatsRespDTO get7DayStats(Long patientId) {
        return getLatestStats(patientId, 7);
    }

    @Override
    public AdherenceStatsRespDTO get30DayStats(Long patientId) {
        return getLatestStats(patientId, 30);
    }

    @Override
    public AdherenceStatsRespDTO get90DayStats(Long patientId) {
        return getLatestStats(patientId, 90);
    }

    @Override
    public boolean needsUpdate(Long patientId, Integer period) {
        MedAdherenceStatsDO latestStats = medAdherenceStatsMapper.selectLatestByPatientIdAndPeriod(patientId, period);
        if (latestStats == null) {
            return true;
        }

        // 检查统计日期是否过期（超过1天）
        LocalDate today = LocalDate.now();
        return latestStats.getStatisticsDate().isBefore(today.minusDays(1));
    }

    /**
     * 计算统计数据
     */
    private AdherenceStatsRespDTO calculateStats(Long patientId, Integer period, LocalDate startDate, LocalDate endDate) {
        // 1. 统计任务数
        Integer totalTasks = medAdherenceStatsMapper.countTotalTasks(patientId, startDate, endDate);
        Integer completedTasks = medAdherenceStatsMapper.countCompletedTasks(patientId, startDate, endDate);
        Integer missedTasks = medAdherenceStatsMapper.countSkippedTasks(patientId, startDate, endDate);
        Integer delayedTasks = 0; // 延迟任务数需要额外计算

        // 2. 计算依从率
        BigDecimal adherenceRate = BigDecimal.ZERO;
        if (totalTasks > 0) {
            adherenceRate = BigDecimal.valueOf(completedTasks)
                .divide(BigDecimal.valueOf(totalTasks), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }

        // 3. 获取常用药品
        List<MedAdherenceStatsMapper.DrugStatsDTO> drugStats = medAdherenceStatsMapper.selectTopMedications(patientId, startDate, endDate);
        List<AdherenceStatsRespDTO.DrugStatsDTO> topMedications = convertDrugStats(drugStats, totalTasks);

        // 4. 构建响应DTO
        AdherenceStatsRespDTO respDTO = new AdherenceStatsRespDTO();
        respDTO.setPatientId(patientId);
        respDTO.setPeriod(period);
        respDTO.setStartDate(startDate);
        respDTO.setEndDate(endDate);
        respDTO.setTotalTasks(totalTasks);
        respDTO.setCompletedTasks(completedTasks);
        respDTO.setMissedTasks(missedTasks);
        respDTO.setDelayedTasks(delayedTasks);
        respDTO.setAdherenceRate(adherenceRate);
        respDTO.setTopMedications(topMedications);

        return respDTO;
    }

    /**
     * 转换药品统计
     */
    private List<AdherenceStatsRespDTO.DrugStatsDTO> convertDrugStats(
            List<MedAdherenceStatsMapper.DrugStatsDTO> drugStats, Integer totalTasks) {
        
        List<AdherenceStatsRespDTO.DrugStatsDTO> result = new ArrayList<>();
        
        for (MedAdherenceStatsMapper.DrugStatsDTO drugStat : drugStats) {
            AdherenceStatsRespDTO.DrugStatsDTO dto = new AdherenceStatsRespDTO.DrugStatsDTO();
            dto.setDrugName(drugStat.getDrugName());
            dto.setCount(drugStat.getCount());
            
            // 计算完成率
            BigDecimal completionRate = BigDecimal.ZERO;
            if (totalTasks > 0) {
                completionRate = BigDecimal.valueOf(drugStat.getCount())
                    .divide(BigDecimal.valueOf(totalTasks), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            }
            dto.setCompletionRate(completionRate);
            
            result.add(dto);
        }
        
        return result;
    }

    /**
     * 转换常用药品为JSON
     */
    private String convertTopMedicationsToJson(List<AdherenceStatsRespDTO.DrugStatsDTO> topMedications) {
        if (topMedications == null || topMedications.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(topMedications);
        } catch (JsonProcessingException e) {
            log.error("转换常用药品为JSON失败", e);
            return null;
        }
    }

    /**
     * 填充额外字段
     */
    private void fillAdditionalFields(AdherenceStatsRespDTO respDTO) {
        // 填充周期名称
        respDTO.setPeriodName(getPeriodName(respDTO.getPeriod()));
        
        // 填充依从率百分比
        if (respDTO.getAdherenceRate() != null) {
            respDTO.setAdherenceRatePercent(respDTO.getAdherenceRate().setScale(2, RoundingMode.HALF_UP) + "%");
        }
        
        // 填充药品完成率百分比
        if (respDTO.getTopMedications() != null) {
            for (AdherenceStatsRespDTO.DrugStatsDTO drugStat : respDTO.getTopMedications()) {
                if (drugStat.getCompletionRate() != null) {
                    drugStat.setCompletionRatePercent(drugStat.getCompletionRate().setScale(2, RoundingMode.HALF_UP) + "%");
                }
            }
        }
    }

    /**
     * 获取周期名称
     */
    private String getPeriodName(Integer period) {
        switch (period) {
            case 7: return "7天";
            case 30: return "30天";
            case 90: return "90天";
            default: return period + "天";
        }
    }

}
