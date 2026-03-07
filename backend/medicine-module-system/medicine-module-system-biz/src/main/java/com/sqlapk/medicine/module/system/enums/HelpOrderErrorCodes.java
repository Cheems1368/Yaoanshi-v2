package com.sqlapk.medicine.module.system.enums;

import com.sqlapk.medicine.framework.common.exception.ErrorCode;

/**
 * 工单系统错误码枚举类
 *
 * 工单相关错误码区间 1-000-000-000 ~ 1-000-000-999
 *
 * @author sqlapk
 */
public interface HelpOrderErrorCodes {

    // ========== 工单基础验证 1-000-000-000 ==========
    ErrorCode HELP_ORDER_TITLE_NOT_EMPTY = new ErrorCode(1_000_000_000, "工单标题不能为空");
    ErrorCode HELP_ORDER_TITLE_TOO_LONG = new ErrorCode(1_000_000_001, "工单标题长度不能超过100个字符");
    ErrorCode HELP_ORDER_DESCRIPTION_NOT_EMPTY = new ErrorCode(1_000_000_002, "工单描述不能为空");
    ErrorCode HELP_ORDER_DESCRIPTION_TOO_LONG = new ErrorCode(1_000_000_003, "工单描述长度不能超过1000个字符");
    ErrorCode HELP_ORDER_INTENDED_DRUGS_TOO_MANY = new ErrorCode(1_000_000_004, "意向药品数量不能超过10个");
    ErrorCode HELP_ORDER_ATTACHMENTS_TOO_MANY = new ErrorCode(1_000_000_005, "附件数量不能超过5个");
    ErrorCode HELP_ORDER_STATUS_REASON_NOT_EMPTY = new ErrorCode(1_000_000_006, "状态更新原因不能为空");
    ErrorCode HELP_ORDER_STATUS_REASON_TOO_LONG = new ErrorCode(1_000_000_007, "状态更新原因长度不能超过500个字符");

    // ========== 分页参数验证 1-000-000-010 ==========
    ErrorCode PAGE_NO_MUST_POSITIVE = new ErrorCode(1_000_000_010, "页码必须为正数");
    ErrorCode PAGE_SIZE_RANGE_ERROR = new ErrorCode(1_000_000_011, "每页大小必须在1-100之间");

    // ========== 时间参数验证 1-000-000-020 ==========
    ErrorCode START_TIME_AFTER_END_TIME = new ErrorCode(1_000_000_020, "开始时间不能晚于结束时间");
    ErrorCode TIME_RANGE_TOO_LONG = new ErrorCode(1_000_000_021, "时间范围不能超过30天");
    ErrorCode TIME_RANGE_PARAM_ERROR = new ErrorCode(1_000_000_022, "时间范围参数格式错误");

    // ========== 用户和工单验证 1-000-000-030 ==========
    ErrorCode USER_ID_NOT_EMPTY = new ErrorCode(1_000_000_030, "用户ID不能为空");
    ErrorCode HELP_ORDER_NOT_EXISTS = new ErrorCode(1_000_000_031, "工单不存在");
    ErrorCode HELP_ORDER_STATUS_NOT_EMPTY = new ErrorCode(1_000_000_032, "工单状态不能为空");

    // ========== 请求参数验证 1-000-000-040 ==========
    ErrorCode REQUEST_PARAM_INVALID_CHAR = new ErrorCode(1_000_000_040, "请求参数包含非法字符");

    // ========== 文件验证 1-000-000-050 ==========
    ErrorCode FILE_SIZE_EXCEEDED = new ErrorCode(1_000_000_050, "文件大小超出限制");
    ErrorCode FILE_FORMAT_NOT_SUPPORTED = new ErrorCode(1_000_000_051, "文件格式不支持");

    // ========== 工单时间限制 1-000-000-060 ==========
    ErrorCode HELP_ORDER_CREATE_TIME_LIMIT = new ErrorCode(1_000_000_060, "工单创建时间不能晚于当前时间");
    ErrorCode HELP_ORDER_PROCESS_TIME_OUT = new ErrorCode(1_000_000_061, "工单处理超时");

    // ========== 工单数量限制 1-000-000-070 ==========
    ErrorCode HELP_ORDER_COUNT_LIMIT = new ErrorCode(1_000_000_070, "工单数量已达上限");

    // ========== 请求频率限制 1-000-000-080 ==========
    ErrorCode REQUEST_RATE_LIMIT_EXCEEDED = new ErrorCode(1_000_000_080, "请求频率超出限制");

    // ========== 数据完整性验证 1-000-000-090 ==========
    ErrorCode DATA_INTEGRITY_CHECK_FAILED = new ErrorCode(1_000_000_090, "数据完整性检查失败");

    // ========== 工单状态相关 1-000-000-100 ==========
    ErrorCode HELP_ORDER_STATUS_INVALID = new ErrorCode(1_000_000_100, "工单状态无效");
    ErrorCode HELP_ORDER_STATUS_TRANSITION_INVALID = new ErrorCode(1_000_000_101, "工单状态转换无效");
    ErrorCode HELP_ORDER_ALREADY_ASSIGNED = new ErrorCode(1_000_000_102, "工单已被分配");
    ErrorCode HELP_ORDER_PERMISSION_DENIED = new ErrorCode(1_000_000_103, "工单权限不足");
    ErrorCode HELP_ORDER_STATUS_PARSE_ERROR = new ErrorCode(1_000_000_104, "工单状态解析错误");

    // ========== 工单操作相关 1-000-000-110 ==========
    ErrorCode HELP_ORDER_EXPORT_FAILED = new ErrorCode(1_000_000_110, "工单导出失败");
    ErrorCode HELP_ORDER_DELETE_FAILED = new ErrorCode(1_000_000_111, "工单删除失败");
    ErrorCode HELP_ORDER_UPDATE_FAILED = new ErrorCode(1_000_000_112, "工单更新失败");
    ErrorCode HELP_ORDER_CREATE_FAILED = new ErrorCode(1_000_000_113, "工单创建失败");

    // ========== 工单分配相关 1-000-000-120 ==========
    ErrorCode HELP_ORDER_PHARMACIST_NOT_EXISTS = new ErrorCode(1_000_000_120, "药师不存在");
    ErrorCode HELP_ORDER_DOCTOR_NOT_EXISTS = new ErrorCode(1_000_000_121, "医生不存在");
    ErrorCode HELP_ORDER_ALREADY_ASSIGNED_PHARMACIST = new ErrorCode(1_000_000_122, "工单已分配给药师");
    ErrorCode HELP_ORDER_ALREADY_ASSIGNED_DOCTOR = new ErrorCode(1_000_000_123, "工单已分配给医生");

    // ========== 工单审核相关 1-000-000-130 ==========
    ErrorCode HELP_ORDER_REJECT_REASON_NOT_EMPTY = new ErrorCode(1_000_000_130, "驳回原因不能为空");
    ErrorCode HELP_ORDER_REJECT_REASON_TOO_LONG = new ErrorCode(1_000_000_131, "驳回原因长度不能超过500个字符");
    ErrorCode HELP_ORDER_APPROVE_FAILED = new ErrorCode(1_000_000_132, "工单审核失败");

    // ========== 工单药品相关 1-000-000-140 ==========
    ErrorCode HELP_ORDER_DRUG_NOT_EXISTS = new ErrorCode(1_000_000_140, "药品不存在");
    ErrorCode HELP_ORDER_DRUG_INVALID = new ErrorCode(1_000_000_141, "药品信息无效");
    ErrorCode HELP_ORDER_DRUG_CONFLICT = new ErrorCode(1_000_000_142, "药品冲突");

    // ========== 工单附件相关 1-000-000-150 ==========
    ErrorCode HELP_ORDER_ATTACHMENT_UPLOAD_FAILED = new ErrorCode(1_000_000_150, "附件上传失败");
    ErrorCode HELP_ORDER_ATTACHMENT_DOWNLOAD_FAILED = new ErrorCode(1_000_000_151, "附件下载失败");
    ErrorCode HELP_ORDER_ATTACHMENT_DELETE_FAILED = new ErrorCode(1_000_000_152, "附件删除失败");

    // ========== 工单通知相关 1-000-000-160 ==========
    ErrorCode HELP_ORDER_NOTIFICATION_SEND_FAILED = new ErrorCode(1_000_000_160, "工单通知发送失败");
    ErrorCode HELP_ORDER_NOTIFICATION_TEMPLATE_NOT_EXISTS = new ErrorCode(1_000_000_161, "通知模板不存在");

    // ========== 工单统计相关 1-000-000-170 ==========
    ErrorCode HELP_ORDER_STATISTICS_FAILED = new ErrorCode(1_000_000_170, "工单统计失败");
    ErrorCode HELP_ORDER_REPORT_GENERATE_FAILED = new ErrorCode(1_000_000_171, "工单报告生成失败");

    // ========== 工单系统相关 1-000-000-180 ==========
    ErrorCode HELP_ORDER_SYSTEM_ERROR = new ErrorCode(1_000_000_180, "工单系统错误");
    ErrorCode HELP_ORDER_SYSTEM_BUSY = new ErrorCode(1_000_000_181, "工单系统繁忙");
    ErrorCode HELP_ORDER_SYSTEM_MAINTENANCE = new ErrorCode(1_000_000_182, "工单系统维护中");
}