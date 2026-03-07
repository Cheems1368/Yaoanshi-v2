package com.sqlapk.medicine.module.system.validation;

import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderCreateReqVO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderUpdateReqVO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderStatusUpdateReqVO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderExportReqVO;
import com.sqlapk.medicine.module.system.enums.HelpOrderStatus;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工单验证器实现
 * 实现具体的业务规则验证逻辑
 *
 * @author sqlapk
 */
@Slf4j
public class HelpOrderValidator implements ConstraintValidator<HelpOrderValidation, Object> {

    private HelpOrderValidation.ValidationType validationType;

    @Override
    public void initialize(HelpOrderValidation constraintAnnotation) {
        this.validationType = constraintAnnotation.type();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // 让@NotNull等注解处理空值验证
        }

        try {
            switch (validationType) {
                case CREATE:
                    return validateCreate((HelpOrderCreateReqVO) value, context);
                case UPDATE:
                    return validateUpdate((HelpOrderUpdateReqVO) value, context);
                case STATUS:
                    return validateStatusUpdate((HelpOrderStatusUpdateReqVO) value, context);
                case ASSIGN:
                    return validateAssign(value, context);
                case EXPORT:
                    return validateExport((HelpOrderExportReqVO) value, context);
                default:
                    return true;
            }
        } catch (Exception e) {
            log.error("工单验证过程中发生异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证创建工单请求
     */
    private boolean validateCreate(HelpOrderCreateReqVO reqVO, ConstraintValidatorContext context) {
        boolean isValid = true;

        // 验证标题长度
        if (reqVO.getTitle() != null && reqVO.getTitle().length() > 100) {
            addConstraintViolation(context, "工单标题长度不能超过100个字符", "title");
            isValid = false;
        }

        // 验证描述长度
        if (reqVO.getDescription() != null && reqVO.getDescription().length() > 1000) {
            addConstraintViolation(context, "工单描述长度不能超过1000个字符", "description");
            isValid = false;
        }

        // 验证意向药品数量
        if (reqVO.getIntendedDrugs() != null && reqVO.getIntendedDrugs().size() > 10) {
            addConstraintViolation(context, "意向药品数量不能超过10个", "intendedDrugs");
            isValid = false;
        }

        // 验证附件数量
        if (reqVO.getAttachments() != null && reqVO.getAttachments().size() > 5) {
            addConstraintViolation(context, "附件数量不能超过5个", "attachments");
            isValid = false;
        }

        // 验证患者ID
        if (reqVO.getPatientId() == null) {
            addConstraintViolation(context, "患者ID不能为空", "patientId");
            isValid = false;
        }

        log.debug("创建工单验证结果: {}", isValid);
        return isValid;
    }

    /**
     * 验证更新工单请求
     */
    private boolean validateUpdate(HelpOrderUpdateReqVO reqVO, ConstraintValidatorContext context) {
        boolean isValid = true;

        // 验证工单ID
        if (reqVO.getId() == null) {
            addConstraintViolation(context, "工单ID不能为空", "id");
            isValid = false;
        }

        // 验证标题长度
        if (reqVO.getTitle() != null && reqVO.getTitle().length() > 100) {
            addConstraintViolation(context, "工单标题长度不能超过100个字符", "title");
            isValid = false;
        }

        // 验证描述长度
        if (reqVO.getDescription() != null && reqVO.getDescription().length() > 1000) {
            addConstraintViolation(context, "工单描述长度不能超过1000个字符", "description");
            isValid = false;
        }

        // 验证意向药品数量
        if (reqVO.getIntendedDrugs() != null && reqVO.getIntendedDrugs().size() > 10) {
            addConstraintViolation(context, "意向药品数量不能超过10个", "intendedDrugs");
            isValid = false;
        }

        // 验证附件数量
        if (reqVO.getAttachments() != null && reqVO.getAttachments().size() > 5) {
            addConstraintViolation(context, "附件数量不能超过5个", "attachments");
            isValid = false;
        }

        log.debug("更新工单验证结果: {}", isValid);
        return isValid;
    }

    /**
     * 验证状态更新请求
     */
    private boolean validateStatusUpdate(HelpOrderStatusUpdateReqVO reqVO, ConstraintValidatorContext context) {
        boolean isValid = true;

        // 验证工单ID
        if (reqVO.getId() == null) {
            addConstraintViolation(context, "工单ID不能为空", "id");
            isValid = false;
        }

        // 验证状态
        if (reqVO.getStatus() == null || reqVO.getStatus().trim().isEmpty()) {
            addConstraintViolation(context, "工单状态不能为空", "status");
            isValid = false;
        } else {
            // 验证状态值是否有效
            try {
                HelpOrderStatus.fromCode(reqVO.getStatus());
            } catch (IllegalArgumentException e) {
                addConstraintViolation(context, "无效的工单状态: " + reqVO.getStatus(), "status");
                isValid = false;
            }
        }

        // 验证原因长度
        if (reqVO.getReason() != null && reqVO.getReason().length() > 500) {
            addConstraintViolation(context, "状态更新原因长度不能超过500个字符", "reason");
            isValid = false;
        }

        log.debug("状态更新验证结果: {}", isValid);
        return isValid;
    }

    /**
     * 验证分配请求
     */
    private boolean validateAssign(Object value, ConstraintValidatorContext context) {
        boolean isValid = true;

        // 这里可以根据具体的分配请求类型进行验证
        // 例如：分配药师、分配医生等

        log.debug("分配验证结果: {}", isValid);
        return isValid;
    }

    /**
     * 验证导出请求
     */
    private boolean validateExport(HelpOrderExportReqVO reqVO, ConstraintValidatorContext context) {
        boolean isValid = true;

        // 验证时间范围
        if (reqVO.getCreateTime() != null && reqVO.getCreateTime().length == 2) {
            LocalDateTime startTime = reqVO.getCreateTime()[0];
            LocalDateTime endTime = reqVO.getCreateTime()[1];
            
            if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
                addConstraintViolation(context, "开始时间不能晚于结束时间", "createTime");
                isValid = false;
            }
            
            // 验证时间范围不能超过30天
            if (startTime != null && endTime != null && 
                startTime.plusDays(30).isBefore(endTime)) {
                addConstraintViolation(context, "时间范围不能超过30天", "createTime");
                isValid = false;
            }
        }

        // 验证状态
        if (reqVO.getStatus() != null && !reqVO.getStatus().trim().isEmpty()) {
            try {
                HelpOrderStatus.fromCode(reqVO.getStatus());
            } catch (IllegalArgumentException e) {
                addConstraintViolation(context, "无效的工单状态: " + reqVO.getStatus(), "status");
                isValid = false;
            }
        }

        // 验证标题长度
        if (reqVO.getTitle() != null && reqVO.getTitle().length() > 100) {
            addConstraintViolation(context, "搜索标题长度不能超过100个字符", "title");
            isValid = false;
        }

        log.debug("导出验证结果: {}", isValid);
        return isValid;
    }

    /**
     * 添加约束违反信息
     */
    private void addConstraintViolation(ConstraintValidatorContext context, String message, String propertyPath) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
               .addPropertyNode(propertyPath)
               .addConstraintViolation();
    }
}
