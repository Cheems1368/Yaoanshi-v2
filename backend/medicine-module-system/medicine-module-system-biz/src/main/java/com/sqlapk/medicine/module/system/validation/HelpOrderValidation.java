package com.sqlapk.medicine.module.system.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 工单验证注解
 * 用于验证工单相关的业务规则
 *
 * @author sqlapk
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HelpOrderValidator.class)
@Documented
public @interface HelpOrderValidation {
    
    String message() default "工单验证失败";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * 验证类型
     */
    ValidationType type() default ValidationType.CREATE;
    
    enum ValidationType {
        CREATE,     // 创建工单验证
        UPDATE,     // 更新工单验证
        STATUS,     // 状态更新验证
        ASSIGN,     // 分配验证
        EXPORT      // 导出验证
    }
}
