package com.sqlapk.medicine.module.medication.service;

import com.sqlapk.medicine.framework.common.exception.ServiceException;
import com.sqlapk.medicine.module.medication.api.dto.MedPlanCreateReqDTO;
import com.sqlapk.medicine.module.medication.api.dto.MedPlanItemCreateReqDTO;
import com.sqlapk.medicine.module.medication.enums.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 用药计划合法性校验服务
 * 
 * 校验规则冲突、起止日期、依从期跨度限制等
 *
 * @author sqlapk
 */
@Service
@Slf4j
public class MedPlanValidationService {

    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]?[0-9]|2[0-3]):[0-5][0-9]$");
    private static final Pattern DOSAGE_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?(mg|g|ml|片|粒|袋|支)$");
    
    /**
     * 校验用药计划创建请求
     */
    public void validateMedPlanCreate(MedPlanCreateReqDTO createReqDTO) {
        log.info("校验用药计划创建请求，患者ID: {}", createReqDTO.getPatientId());

        // 1. 基础字段校验
        validateBasicFields(createReqDTO);

        // 2. 日期校验
        validateDates(createReqDTO.getStartDate(), createReqDTO.getEndDate());

        // 3. 计划项校验
        validatePlanItems(createReqDTO.getItems());

        // 4. 业务规则校验
        validateBusinessRules(createReqDTO);

        log.info("用药计划创建请求校验通过");
    }

    /**
     * 校验基础字段
     */
    private void validateBasicFields(MedPlanCreateReqDTO createReqDTO) {
        if (createReqDTO.getPatientId() == null || createReqDTO.getPatientId() <= 0) {
            throw new ServiceException(ErrorCodeConstants.BAD_REQUEST.getCode(), "患者ID不能为空");
        }

        if (createReqDTO.getPlanName() == null || createReqDTO.getPlanName().trim().isEmpty()) {
            throw new ServiceException(ErrorCodeConstants.BAD_REQUEST.getCode(), "计划名称不能为空");
        }

        if (createReqDTO.getPlanName().length() > 128) {
            throw new ServiceException(ErrorCodeConstants.BAD_REQUEST.getCode(), "计划名称长度不能超过128个字符");
        }

        if (createReqDTO.getItems() == null || createReqDTO.getItems().isEmpty()) {
            throw new ServiceException(ErrorCodeConstants.BAD_REQUEST.getCode(), "用药计划项不能为空");
        }

        if (createReqDTO.getItems().size() > 20) {
            throw new ServiceException(ErrorCodeConstants.BAD_REQUEST.getCode(), "用药计划项数量不能超过20个");
        }
    }

    /**
     * 校验日期
     */
    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new ServiceException(ErrorCodeConstants.BAD_REQUEST.getCode(), "开始日期不能为空");
        }

        // 开始日期不能早于今天
        if (startDate.isBefore(LocalDate.now())) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_DATE_INVALID);
        }

        // 结束日期不能早于开始日期
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_DATE_INVALID);
        }

        // 计划周期不能超过1年
        if (endDate != null && startDate.plusYears(1).isBefore(endDate)) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_DATE_INVALID);
        }
    }

    /**
     * 校验计划项
     */
    private void validatePlanItems(List<MedPlanItemCreateReqDTO> items) {
        for (int i = 0; i < items.size(); i++) {
            MedPlanItemCreateReqDTO item = items.get(i);
            validatePlanItem(item, i + 1);
        }

        // 检查药品名称重复
        long distinctDrugNames = items.stream()
            .map(MedPlanItemCreateReqDTO::getDrugName)
            .distinct()
            .count();
        
        if (distinctDrugNames != items.size()) {
            throw new ServiceException(ErrorCodeConstants.MED_PLAN_ITEM_DUPLICATE);
        }
    }

    /**
     * 校验单个计划项
     */
    private void validatePlanItem(MedPlanItemCreateReqDTO item, int index) {
        // 药品名称校验
        if (item.getDrugName() == null || item.getDrugName().trim().isEmpty()) {
            throw new ServiceException(ErrorCodeConstants.BAD_REQUEST.getCode(), 
                String.format("第%d个计划项的药品名称不能为空", index));
        }

        if (item.getDrugName().length() > 128) {
            throw new ServiceException(ErrorCodeConstants.BAD_REQUEST.getCode(), 
                String.format("第%d个计划项的药品名称长度不能超过128个字符", index));
        }

        // 剂量校验
        if (item.getDosage() == null || item.getDosage().trim().isEmpty()) {
            throw new ServiceException(ErrorCodeConstants.BAD_REQUEST.getCode(), 
                String.format("第%d个计划项的剂量不能为空", index));
        }

        if (!DOSAGE_PATTERN.matcher(item.getDosage()).matches()) {
            throw new ServiceException(ErrorCodeConstants.DOSAGE_INVALID);
        }

        // 频次校验
        if (item.getFrequency() == null || item.getFrequency().trim().isEmpty()) {
            throw new ServiceException(ErrorCodeConstants.BAD_REQUEST.getCode(), 
                String.format("第%d个计划项的频次不能为空", index));
        }

        // 时间点校验
        if (item.getTimePoints() == null || item.getTimePoints().isEmpty()) {
            throw new ServiceException(ErrorCodeConstants.BAD_REQUEST.getCode(), 
                String.format("第%d个计划项的时间点不能为空", index));
        }

        validateTimePoints(item.getTimePoints(), index);

        // 餐前后关系校验
        if (item.getMealRelation() == null || item.getMealRelation() < 0 || item.getMealRelation() > 3) {
            throw new ServiceException(ErrorCodeConstants.BAD_REQUEST.getCode(), 
                String.format("第%d个计划项的餐前后关系无效", index));
        }
    }

    /**
     * 校验时间点
     */
    private void validateTimePoints(List<String> timePoints, int index) {
        if (timePoints.size() > 6) {
            throw new ServiceException(ErrorCodeConstants.BAD_REQUEST.getCode(), 
                String.format("第%d个计划项的时间点数量不能超过6个", index));
        }

        for (String timePoint : timePoints) {
            if (!TIME_PATTERN.matcher(timePoint).matches()) {
                throw new ServiceException(ErrorCodeConstants.BAD_REQUEST.getCode(), 
                    String.format("第%d个计划项的时间点格式无效: %s", index, timePoint));
            }

            try {
                LocalTime.parse(timePoint, DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception e) {
                throw new ServiceException(ErrorCodeConstants.BAD_REQUEST.getCode(), 
                    String.format("第%d个计划项的时间点无效: %s", index, timePoint));
            }
        }

        // 检查时间点重复
        long distinctTimePoints = timePoints.stream().distinct().count();
        if (distinctTimePoints != timePoints.size()) {
            throw new ServiceException(ErrorCodeConstants.BAD_REQUEST.getCode(), 
                String.format("第%d个计划项的时间点不能重复", index));
        }
    }

    /**
     * 校验业务规则
     */
    private void validateBusinessRules(MedPlanCreateReqDTO createReqDTO) {
        // 检查用药冲突
        validateMedicationConflicts(createReqDTO.getItems());

        // 检查依从期跨度限制
        validateAdherencePeriod(createReqDTO.getStartDate(), createReqDTO.getEndDate());
    }

    /**
     * 校验用药冲突
     */
    private void validateMedicationConflicts(List<MedPlanItemCreateReqDTO> items) {
        // 这里可以添加具体的用药冲突检查逻辑
        // 例如：检查是否有相互作用的药物
        // 检查是否有重复的药物但不同剂量
        
        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                MedPlanItemCreateReqDTO item1 = items.get(i);
                MedPlanItemCreateReqDTO item2 = items.get(j);
                
                // 检查是否有相同时间点的不同药物
                if (hasTimeConflict(item1.getTimePoints(), item2.getTimePoints())) {
                    log.warn("检测到时间冲突: {} 和 {}", item1.getDrugName(), item2.getDrugName());
                    // 这里可以根据业务需求决定是否抛出异常
                }
            }
        }
    }

    /**
     * 检查时间冲突
     */
    private boolean hasTimeConflict(List<String> timePoints1, List<String> timePoints2) {
        return timePoints1.stream().anyMatch(timePoints2::contains);
    }

    /**
     * 校验依从期跨度限制
     */
    private void validateAdherencePeriod(LocalDate startDate, LocalDate endDate) {
        if (endDate != null) {
            long daysBetween = startDate.until(endDate).getDays();
            
            // 依从期跨度不能超过365天
            if (daysBetween > 365) {
                throw new ServiceException(ErrorCodeConstants.ADHERENCE_PERIOD_INVALID);
            }
        }
    }

    /**
     * 校验计划更新请求
     */
    public void validateMedPlanUpdate(Long planId, MedPlanCreateReqDTO updateReqDTO) {
        log.info("校验用药计划更新请求，计划ID: {}", planId);

        if (planId == null || planId <= 0) {
            throw new ServiceException(ErrorCodeConstants.BAD_REQUEST.getCode(), "计划ID不能为空");
        }

        // 复用创建请求的校验逻辑
        validateMedPlanCreate(updateReqDTO);

        log.info("用药计划更新请求校验通过");
    }

}
