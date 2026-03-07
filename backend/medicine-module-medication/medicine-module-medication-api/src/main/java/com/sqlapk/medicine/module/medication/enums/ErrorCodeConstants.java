package com.sqlapk.medicine.module.medication.enums;

import com.sqlapk.medicine.framework.common.exception.ErrorCode;

/**
 * 用药打卡系统错误码枚举类
 * 
 * 用药打卡系统，使用 1-002-000-000 段
 *
 * @author sqlapk
 */
public interface ErrorCodeConstants {

    // ========== 用药计划相关 1-002-001-000 ==========
    ErrorCode MED_PLAN_NOT_EXISTS = new ErrorCode(1_002_001_000, "用药计划不存在");
    ErrorCode MED_PLAN_STATUS_ERROR = new ErrorCode(1_002_001_001, "用药计划状态错误");
    ErrorCode MED_PLAN_DATE_INVALID = new ErrorCode(1_002_001_002, "用药计划日期无效");
    ErrorCode MED_PLAN_ITEM_NOT_EXISTS = new ErrorCode(1_002_001_003, "用药计划项不存在");
    ErrorCode MED_PLAN_ITEM_DUPLICATE = new ErrorCode(1_002_001_004, "用药计划项重复");

    // ========== 今日待办相关 1-002-002-000 ==========
    ErrorCode TODAY_TASK_NOT_EXISTS = new ErrorCode(1_002_002_000, "今日待办任务不存在");
    ErrorCode TODAY_TASK_STATUS_ERROR = new ErrorCode(1_002_002_001, "今日待办任务状态错误");
    ErrorCode TODAY_TASK_ALREADY_COMPLETED = new ErrorCode(1_002_002_002, "今日待办任务已完成");
    ErrorCode TODAY_TASK_GENERATION_FAILED = new ErrorCode(1_002_002_003, "今日待办任务生成失败");

    // ========== 打卡记录相关 1-002-003-000 ==========
    ErrorCode CHECKIN_NOT_EXISTS = new ErrorCode(1_002_003_000, "打卡记录不存在");
    ErrorCode CHECKIN_ALREADY_EXISTS = new ErrorCode(1_002_003_001, "打卡记录已存在");
    ErrorCode CHECKIN_STATUS_ERROR = new ErrorCode(1_002_003_002, "打卡记录状态错误");
    ErrorCode CHECKIN_TIME_INVALID = new ErrorCode(1_002_003_003, "打卡时间无效");
    ErrorCode CHECKIN_REVOKE_FAILED = new ErrorCode(1_002_003_004, "打卡撤销失败");
    ErrorCode CHECKIN_EVIDENCE_INVALID = new ErrorCode(1_002_003_005, "打卡证据无效");

    // ========== 依从统计相关 1-002-004-000 ==========
    ErrorCode ADHERENCE_STATS_NOT_EXISTS = new ErrorCode(1_002_004_000, "依从统计不存在");
    ErrorCode ADHERENCE_STATS_CALCULATION_FAILED = new ErrorCode(1_002_004_001, "依从统计计算失败");
    ErrorCode ADHERENCE_PERIOD_INVALID = new ErrorCode(1_002_004_002, "依从统计周期无效");

    // ========== 报告生成相关 1-002-005-000 ==========
    ErrorCode REPORT_NOT_EXISTS = new ErrorCode(1_002_005_000, "报告不存在");
    ErrorCode REPORT_GENERATION_FAILED = new ErrorCode(1_002_005_001, "报告生成失败");
    ErrorCode REPORT_EXPORT_FAILED = new ErrorCode(1_002_005_002, "报告导出失败");
    ErrorCode REPORT_TYPE_INVALID = new ErrorCode(1_002_005_003, "报告类型无效");

    // ========== 通知提醒相关 1-002-006-000 ==========
    ErrorCode NOTIFICATION_NOT_EXISTS = new ErrorCode(1_002_006_000, "通知不存在");
    ErrorCode NOTIFICATION_SEND_FAILED = new ErrorCode(1_002_006_001, "通知发送失败");
    ErrorCode NOTIFICATION_TEMPLATE_NOT_EXISTS = new ErrorCode(1_002_006_002, "通知模板不存在");
    ErrorCode NOTIFICATION_CHANNEL_INVALID = new ErrorCode(1_002_006_003, "通知渠道无效");
    ErrorCode NOTIFICATION_CHANNEL_NOT_SUPPORTED = new ErrorCode(1_002_006_004, "通知渠道不支持");

    // ========== 患者档案相关 1-002-007-000 ==========
    ErrorCode PATIENT_PROFILE_NOT_EXISTS = new ErrorCode(1_002_007_000, "患者档案不存在");
    ErrorCode PATIENT_PROFILE_INCOMPLETE = new ErrorCode(1_002_007_001, "患者档案不完整");
    ErrorCode PATIENT_PROFILE_UPDATE_FAILED = new ErrorCode(1_002_007_002, "患者档案更新失败");

    // ========== 权限相关 1-002-008-000 ==========
    ErrorCode PERMISSION_DENIED = new ErrorCode(1_002_008_000, "权限不足");
    ErrorCode ROLE_NOT_MATCH = new ErrorCode(1_002_008_001, "角色不匹配");
    ErrorCode PATIENT_ACCESS_DENIED = new ErrorCode(1_002_008_002, "患者访问被拒绝");

    // ========== 业务规则相关 1-002-009-000 ==========
    ErrorCode BUSINESS_RULE_VIOLATION = new ErrorCode(1_002_009_000, "业务规则违反");
    ErrorCode MEDICATION_CONFLICT = new ErrorCode(1_002_009_001, "用药冲突");
    ErrorCode DOSAGE_INVALID = new ErrorCode(1_002_009_002, "剂量无效");
    ErrorCode FREQUENCY_INVALID = new ErrorCode(1_002_009_003, "频次无效");

    // ========== 通用错误 1-002-010-000 ==========
    ErrorCode BAD_REQUEST = new ErrorCode(1_002_010_000, "请求参数错误");

}
