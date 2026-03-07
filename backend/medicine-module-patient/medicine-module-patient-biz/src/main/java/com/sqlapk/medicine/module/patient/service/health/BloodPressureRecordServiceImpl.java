package com.sqlapk.medicine.module.patient.service.health;

import com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil;
import com.sqlapk.medicine.module.patient.api.BloodPressureRecordApi;
import com.sqlapk.medicine.module.patient.api.dto.health.BloodPressureRecordReqDTO;
import com.sqlapk.medicine.module.patient.api.dto.health.BloodPressureRecordRespDTO;
import com.sqlapk.medicine.module.patient.api.dto.health.BloodPressureTrendRespDTO;
import com.sqlapk.medicine.module.patient.convert.health.BloodPressureRecordConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.health.BloodPressureRecordDO;
import com.sqlapk.medicine.module.patient.dal.mysql.health.BloodPressureRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 血压记录服务实现
 *
 * @author sqlapk
 */
@Service
@Slf4j
public class BloodPressureRecordServiceImpl implements BloodPressureRecordApi {

    @Autowired
    private BloodPressureRecordMapper bloodPressureRecordMapper;

    @Override
    @Transactional
    public Long createBloodPressureRecord(BloodPressureRecordReqDTO reqDTO) {
        log.info("创建血压记录，患者ID: {}, 收缩压: {}, 舒张压: {}", 
                reqDTO.getPatientId(), reqDTO.getSystolicPressure(), reqDTO.getDiastolicPressure());

        // 1. 数据验证
        validateBloodPressureData(reqDTO);

        // 2. 转换为DO对象
        BloodPressureRecordDO recordDO = BloodPressureRecordConvert.INSTANCE.convert(reqDTO);
        
        // 3. 设置默认值
        if (recordDO.getMeasureTime() == null) {
            recordDO.setMeasureTime(LocalDateTime.now());
        }
        if (recordDO.getDataSource() == null) {
            recordDO.setDataSource("手动录入");
        }
        if (recordDO.getMeasureStatus() == null) {
            recordDO.setMeasureStatus(0);
        }

        // 4. 判断是否异常
        BloodPressureAnalysis analysis = analyzeBloodPressure(reqDTO.getSystolicPressure(), reqDTO.getDiastolicPressure());
        recordDO.setIsAbnormal(analysis.isAbnormal ? 1 : 0);
        recordDO.setAbnormalType(analysis.abnormalType);

        // 5. 保存记录
        bloodPressureRecordMapper.insert(recordDO);

        log.info("血压记录创建成功，记录ID: {}", recordDO.getId());
        return recordDO.getId();
    }

    @Override
    public List<BloodPressureRecordRespDTO> getRecentBloodPressureRecords(Long patientId, Integer limit) {
        log.info("获取患者最近血压记录，患者ID: {}, 限制数量: {}", patientId, limit);

        List<BloodPressureRecordDO> records = bloodPressureRecordMapper.selectRecentRecords(patientId, limit);
        return records.stream()
                .map(BloodPressureRecordConvert.INSTANCE::convert)
                .collect(Collectors.toList());
    }

    @Override
    public BloodPressureTrendRespDTO getBloodPressureTrend(Long patientId, Integer days) {
        log.info("获取患者血压趋势，患者ID: {}, 统计天数: {}", patientId, days);

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        // 1. 获取统计数据
        BloodPressureRecordMapper.BloodPressureStats stats = 
                bloodPressureRecordMapper.selectBloodPressureStats(patientId, startTime, endTime);

        // 2. 获取详细记录
        List<BloodPressureRecordDO> records = 
                bloodPressureRecordMapper.selectRecordsByTimeRange(patientId, startTime, endTime);

        // 3. 构建趋势数据
        BloodPressureTrendRespDTO trendDTO = new BloodPressureTrendRespDTO();
        trendDTO.setPatientId(patientId);
        trendDTO.setPeriodDays(days);
        
        if (stats != null) {
            trendDTO.setAvgSystolicPressure(stats.getAvgSystolic());
            trendDTO.setAvgDiastolicPressure(stats.getAvgDiastolic());
            trendDTO.setAvgHeartRate(stats.getAvgHeartRate());
            trendDTO.setRecordCount(stats.getRecordCount());
        }

        // 4. 计算极值
        if (!records.isEmpty()) {
            int maxSystolic = records.stream().mapToInt(BloodPressureRecordDO::getSystolicPressure).max().orElse(0);
            int minSystolic = records.stream().mapToInt(BloodPressureRecordDO::getSystolicPressure).min().orElse(0);
            int maxDiastolic = records.stream().mapToInt(BloodPressureRecordDO::getDiastolicPressure).max().orElse(0);
            int minDiastolic = records.stream().mapToInt(BloodPressureRecordDO::getDiastolicPressure).min().orElse(0);
            
            trendDTO.setMaxSystolicPressure(maxSystolic);
            trendDTO.setMinSystolicPressure(minSystolic);
            trendDTO.setMaxDiastolicPressure(maxDiastolic);
            trendDTO.setMinDiastolicPressure(minDiastolic);
            
            // 计算异常记录数
            long abnormalCount = records.stream()
                    .mapToLong(r -> r.getIsAbnormal() != null && r.getIsAbnormal() == 1 ? 1 : 0)
                    .sum();
            trendDTO.setAbnormalCount(abnormalCount);
        }

        // 5. 构建趋势数据点
        List<BloodPressureTrendRespDTO.TrendDataPoint> trendData = buildTrendDataPoints(records);
        trendDTO.setTrendData(trendData);

        return trendDTO;
    }

    @Override
    public BloodPressureTrendRespDTO getBloodPressureStats(Long patientId, Integer days) {
        return getBloodPressureTrend(patientId, days);
    }

    @Override
    @Transactional
    public List<Long> batchCreateBloodPressureRecords(List<BloodPressureRecordReqDTO> reqDTOList) {
        log.info("批量创建血压记录，记录数量: {}", reqDTOList.size());

        List<Long> createdIds = new ArrayList<>();
        for (BloodPressureRecordReqDTO reqDTO : reqDTOList) {
            try {
                Long recordId = createBloodPressureRecord(reqDTO);
                createdIds.add(recordId);
            } catch (Exception e) {
                log.error("批量创建血压记录失败，患者ID: {}", reqDTO.getPatientId(), e);
                // 继续处理其他记录
            }
        }

        log.info("批量创建血压记录完成，成功创建: {} 条", createdIds.size());
        return createdIds;
    }

    /**
     * 验证血压数据
     */
    private void validateBloodPressureData(BloodPressureRecordReqDTO reqDTO) {
        if (reqDTO.getSystolicPressure() < reqDTO.getDiastolicPressure()) {
            throw ServiceExceptionUtil.exception(400, "收缩压不能低于舒张压");
        }
        
        if (reqDTO.getSystolicPressure() > 250 || reqDTO.getSystolicPressure() < 60) {
            throw ServiceExceptionUtil.exception(400, "收缩压数值异常");
        }
        
        if (reqDTO.getDiastolicPressure() > 150 || reqDTO.getDiastolicPressure() < 40) {
            throw ServiceExceptionUtil.exception(400, "舒张压数值异常");
        }
    }

    /**
     * 分析血压是否异常
     */
    private BloodPressureAnalysis analyzeBloodPressure(Integer systolic, Integer diastolic) {
        BloodPressureAnalysis analysis = new BloodPressureAnalysis();
        
        // 根据WHO标准判断血压等级
        if (systolic >= 180 || diastolic >= 110) {
            analysis.isAbnormal = true;
            analysis.abnormalType = "高血压3级";
        } else if (systolic >= 160 || diastolic >= 100) {
            analysis.isAbnormal = true;
            analysis.abnormalType = "高血压2级";
        } else if (systolic >= 140 || diastolic >= 90) {
            analysis.isAbnormal = true;
            analysis.abnormalType = "高血压1级";
        } else if (systolic < 90 && diastolic < 60) {
            analysis.isAbnormal = true;
            analysis.abnormalType = "低血压";
        } else {
            analysis.isAbnormal = false;
            analysis.abnormalType = "正常";
        }
        
        return analysis;
    }

    /**
     * 构建趋势数据点
     */
    private List<BloodPressureTrendRespDTO.TrendDataPoint> buildTrendDataPoints(List<BloodPressureRecordDO> records) {
        return records.stream().map(record -> {
            BloodPressureTrendRespDTO.TrendDataPoint point = new BloodPressureTrendRespDTO.TrendDataPoint();
            point.setDate(record.getMeasureTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            point.setSystolicPressure(record.getSystolicPressure());
            point.setDiastolicPressure(record.getDiastolicPressure());
            point.setHeartRate(record.getHeartRate());
            point.setIsAbnormal(record.getIsAbnormal() != null && record.getIsAbnormal() == 1);
            return point;
        }).collect(Collectors.toList());
    }

    /**
     * 血压分析结果
     */
    private static class BloodPressureAnalysis {
        boolean isAbnormal;
        String abnormalType;
    }
}
