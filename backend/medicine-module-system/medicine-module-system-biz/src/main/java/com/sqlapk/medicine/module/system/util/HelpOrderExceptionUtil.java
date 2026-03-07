// 创建文件: HelpOrderExceptionUtil.java
package com.sqlapk.medicine.module.system.util;

import com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil;
import com.sqlapk.medicine.module.system.enums.HelpOrderErrorCodes;
import com.sqlapk.medicine.module.system.enums.HelpOrderStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * 工单异常处理工具类
 *
 * @author sqlapk
 */
@Slf4j
public class HelpOrderExceptionUtil {

    /**
     * 安全的状态解析
     */
    public static HelpOrderStatus safeParseStatus(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return null;
        }

        try {
            return HelpOrderStatus.fromCode(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            try {
                int ordinal = Integer.parseInt(statusStr);
                if (ordinal >= 0 && ordinal < HelpOrderStatus.values().length) {
                    return HelpOrderStatus.values()[ordinal];
                }
            } catch (NumberFormatException ex) {
                // 忽略解析错误
            }

            log.warn("状态解析失败: {}", statusStr);
            throw ServiceExceptionUtil.exception(HelpOrderErrorCodes.HELP_ORDER_STATUS_PARSE_ERROR);
        }
    }
}
