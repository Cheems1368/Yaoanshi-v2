package com.sqlapk.medicine.module.system.enums;

import com.sqlapk.medicine.framework.common.exception.ErrorCode;

/**
 * 错误码枚举类
 *
 * 系统错误码区间 1-000-000-000 ~ 1-999-999-999
 *
 * @author sqlapk
 */
public interface ErrorCodeConstants {

    // ========== 工单相关 1-000-000-000 ==========
    ErrorCode HELP_ORDER_NOT_EXISTS = new ErrorCode(1_000_000_000, "工单不存在");
    ErrorCode HELP_ORDER_STATUS_INVALID = new ErrorCode(1_000_000_001, "工单状态转换无效");
    ErrorCode HELP_ORDER_PERMISSION_DENIED = new ErrorCode(1_000_000_002, "工单权限不足");
    ErrorCode HELP_ORDER_ALREADY_ASSIGNED = new ErrorCode(1_000_000_003, "工单已被分配");
    ErrorCode HELP_ORDER_PARAMETER_INVALID = new ErrorCode(1_000_000_004, "工单参数验证失败");

    // ========== 用户相关 1-000-001-000 ==========
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1_000_001_000, "用户不存在");
    ErrorCode USER_PASSWORD_ERROR = new ErrorCode(1_000_001_001, "用户密码错误");
    ErrorCode USER_ACCOUNT_DISABLED = new ErrorCode(1_000_001_002, "用户账号已禁用");
    ErrorCode USER_ACCOUNT_EXPIRED = new ErrorCode(1_000_001_003, "用户账号已过期");
    ErrorCode USER_ACCOUNT_LOCKED = new ErrorCode(1_000_001_004, "用户账号已锁定");

    // ========== 角色相关 1-000-002-000 ==========
    ErrorCode ROLE_NOT_EXISTS = new ErrorCode(1_000_002_000, "角色不存在");
    ErrorCode ROLE_NAME_DUPLICATE = new ErrorCode(1_000_002_001, "角色名称重复");
    ErrorCode ROLE_CODE_DUPLICATE = new ErrorCode(1_000_002_002, "角色编码重复");

    // ========== 菜单相关 1-000-003-000 ==========
    ErrorCode MENU_NOT_EXISTS = new ErrorCode(1_000_003_000, "菜单不存在");
    ErrorCode MENU_NAME_DUPLICATE = new ErrorCode(1_000_003_001, "菜单名称重复");
    ErrorCode MENU_PARENT_ERROR = new ErrorCode(1_000_003_002, "菜单父级错误");

    // ========== 部门相关 1-000-004-000 ==========
    ErrorCode DEPT_NOT_EXISTS = new ErrorCode(1_000_004_000, "部门不存在");
    ErrorCode DEPT_NAME_DUPLICATE = new ErrorCode(1_000_004_001, "部门名称重复");
    ErrorCode DEPT_PARENT_ERROR = new ErrorCode(1_000_004_002, "部门父级错误");

    // ========== 岗位相关 1-000-005-000 ==========
    ErrorCode POST_NOT_EXISTS = new ErrorCode(1_000_005_000, "岗位不存在");
    ErrorCode POST_NAME_DUPLICATE = new ErrorCode(1_000_005_001, "岗位名称重复");
    ErrorCode POST_CODE_DUPLICATE = new ErrorCode(1_000_005_002, "岗位编码重复");

    // ========== 字典相关 1-000-006-000 ==========
    ErrorCode DICT_TYPE_NOT_EXISTS = new ErrorCode(1_000_006_000, "字典类型不存在");
    ErrorCode DICT_TYPE_NAME_DUPLICATE = new ErrorCode(1_000_006_001, "字典类型名称重复");
    ErrorCode DICT_TYPE_TYPE_DUPLICATE = new ErrorCode(1_000_006_002, "字典类型编码重复");
    ErrorCode DICT_DATA_NOT_EXISTS = new ErrorCode(1_000_006_003, "字典数据不存在");
    ErrorCode DICT_DATA_VALUE_DUPLICATE = new ErrorCode(1_000_006_004, "字典数据值重复");

    // ========== 通知公告相关 1-000-007-000 ==========
    ErrorCode NOTICE_NOT_EXISTS = new ErrorCode(1_000_007_000, "通知公告不存在");

    // ========== 操作日志相关 1-000-008-000 ==========
    ErrorCode OPERATE_LOG_NOT_EXISTS = new ErrorCode(1_000_008_000, "操作日志不存在");

    // ========== 登录日志相关 1-000-009-000 ==========
    ErrorCode LOGIN_LOG_NOT_EXISTS = new ErrorCode(1_000_009_000, "登录日志不存在");

    // ========== 文件相关 1-000-010-000 ==========
    ErrorCode FILE_NOT_EXISTS = new ErrorCode(1_000_010_000, "文件不存在");
    ErrorCode FILE_UPLOAD_ERROR = new ErrorCode(1_000_010_001, "文件上传失败");
    ErrorCode FILE_DOWNLOAD_ERROR = new ErrorCode(1_000_010_002, "文件下载失败");

    // ========== 配置相关 1-000-011-000 ==========
    ErrorCode CONFIG_NOT_EXISTS = new ErrorCode(1_000_011_000, "配置不存在");
    ErrorCode CONFIG_KEY_DUPLICATE = new ErrorCode(1_000_011_001, "配置键重复");

    // ========== 数据源相关 1-000-012-000 ==========
    ErrorCode DATA_SOURCE_NOT_EXISTS = new ErrorCode(1_000_012_000, "数据源不存在");
    ErrorCode DATA_SOURCE_NAME_DUPLICATE = new ErrorCode(1_000_012_001, "数据源名称重复");

    // ========== 租户相关 1-000-013-000 ==========
    ErrorCode TENANT_NOT_EXISTS = new ErrorCode(1_000_013_000, "租户不存在");
    ErrorCode TENANT_NAME_DUPLICATE = new ErrorCode(1_000_013_001, "租户名称重复");
    ErrorCode TENANT_EXPIRED = new ErrorCode(1_000_013_002, "租户已过期");

    // ========== 短信相关 1-000-014-000 ==========
    ErrorCode SMS_TEMPLATE_NOT_EXISTS = new ErrorCode(1_000_014_000, "短信模板不存在");
    ErrorCode SMS_TEMPLATE_CODE_DUPLICATE = new ErrorCode(1_000_014_001, "短信模板编码重复");
    ErrorCode SMS_SEND_ERROR = new ErrorCode(1_000_014_002, "短信发送失败");

    // ========== 邮件相关 1-000-015-000 ==========
    ErrorCode MAIL_TEMPLATE_NOT_EXISTS = new ErrorCode(1_000_015_000, "邮件模板不存在");
    ErrorCode MAIL_TEMPLATE_CODE_DUPLICATE = new ErrorCode(1_000_015_001, "邮件模板编码重复");
    ErrorCode MAIL_SEND_ERROR = new ErrorCode(1_000_015_002, "邮件发送失败");

    // ========== 定时任务相关 1-000-016-000 ==========
    ErrorCode JOB_NOT_EXISTS = new ErrorCode(1_000_016_000, "定时任务不存在");
    ErrorCode JOB_HANDLER_EXISTS = new ErrorCode(1_000_016_001, "定时任务处理器已存在");
    ErrorCode JOB_UPDATE_ERROR = new ErrorCode(1_000_016_002, "定时任务更新失败");

    // ========== 消息相关 1-000-017-000 ==========
    ErrorCode MESSAGE_TEMPLATE_NOT_EXISTS = new ErrorCode(1_000_017_000, "消息模板不存在");
    ErrorCode MESSAGE_TEMPLATE_CODE_DUPLICATE = new ErrorCode(1_000_017_001, "消息模板编码重复");
    ErrorCode MESSAGE_SEND_ERROR = new ErrorCode(1_000_017_002, "消息发送失败");

    // ========== 系统错误码 1-000-018-000 ==========
    ErrorCode USER_USERNAME_EXISTS = new ErrorCode(1_000_018_000, "用户名已存在");
    ErrorCode USER_MOBILE_EXISTS = new ErrorCode(1_000_018_001, "手机号已存在");
    ErrorCode USER_EMAIL_EXISTS = new ErrorCode(1_000_018_002, "邮箱已存在");
    ErrorCode USER_IS_DISABLE = new ErrorCode(1_000_018_003, "用户已禁用");
    ErrorCode USER_PASSWORD_FAILED = new ErrorCode(1_000_018_004, "密码错误");
    ErrorCode USER_COUNT_MAX = new ErrorCode(1_000_018_005, "用户数量已达上限");
    ErrorCode USER_IMPORT_LIST_IS_EMPTY = new ErrorCode(1_000_018_006, "导入用户数据为空");

    // ========== 部门错误码 1-000-019-000 ==========
    ErrorCode DEPT_NOT_FOUND = new ErrorCode(1_000_019_000, "部门不存在");
    ErrorCode DEPT_NOT_ENABLE = new ErrorCode(1_000_019_001, "部门未启用");
    ErrorCode DEPT_EXITS_CHILDREN = new ErrorCode(1_000_019_002, "部门存在子部门");
    ErrorCode DEPT_PARENT_NOT_EXITS = new ErrorCode(1_000_019_003, "父部门不存在");
    ErrorCode DEPT_PARENT_IS_CHILD = new ErrorCode(1_000_019_004, "父部门不能是子部门");

    // ========== 岗位错误码 1-000-020-000 ==========
    ErrorCode POST_NOT_FOUND = new ErrorCode(1_000_020_000, "岗位不存在");
    ErrorCode POST_NOT_ENABLE = new ErrorCode(1_000_020_001, "岗位未启用");

    // ========== 角色错误码 1-000-021-000 ==========
    ErrorCode ROLE_IS_DISABLE = new ErrorCode(1_000_021_000, "角色已禁用");
    ErrorCode ROLE_ADMIN_CODE_ERROR = new ErrorCode(1_000_021_001, "管理员角色编码错误");
    ErrorCode ROLE_CAN_NOT_UPDATE_SYSTEM_TYPE_ROLE = new ErrorCode(1_000_021_002, "不能修改系统角色");

    // ========== 菜单错误码 1-000-022-000 ==========
    ErrorCode MENU_EXISTS_CHILDREN = new ErrorCode(1_000_022_000, "菜单存在子菜单");
    ErrorCode MENU_PARENT_NOT_EXISTS = new ErrorCode(1_000_022_001, "父菜单不存在");
    ErrorCode MENU_PARENT_NOT_DIR_OR_MENU = new ErrorCode(1_000_022_002, "父菜单类型错误");

    // ========== 字典错误码 1-000-023-000 ==========
    ErrorCode DICT_TYPE_HAS_CHILDREN = new ErrorCode(1_000_023_000, "字典类型存在子数据");
    ErrorCode DICT_DATA_NOT_ENABLE = new ErrorCode(1_000_023_001, "字典数据未启用");
    ErrorCode DICT_TYPE_NOT_ENABLE = new ErrorCode(1_000_023_002, "字典类型未启用");

    // ========== 短信错误码 1-000-024-000 ==========
    ErrorCode SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_DAY = new ErrorCode(1_000_024_000, "短信发送超过每日限制");
    ErrorCode SMS_CODE_EXPIRED = new ErrorCode(1_000_024_001, "短信验证码已过期");
    ErrorCode SMS_CODE_NOT_FOUND = new ErrorCode(1_000_024_002, "短信验证码不存在");
    ErrorCode SMS_CODE_SEND_TOO_FAST = new ErrorCode(1_000_024_003, "短信发送过于频繁");
    ErrorCode SMS_CODE_USED = new ErrorCode(1_000_024_004, "短信验证码已使用");
    ErrorCode SMS_CHANNEL_HAS_CHILDREN = new ErrorCode(1_000_024_005, "短信渠道存在子数据");
    ErrorCode SMS_CHANNEL_NOT_EXISTS = new ErrorCode(1_000_024_006, "短信渠道不存在");
    ErrorCode SMS_CHANNEL_DISABLE = new ErrorCode(1_000_024_007, "短信渠道已禁用");
    ErrorCode SMS_SEND_MOBILE_NOT_EXISTS = new ErrorCode(1_000_024_008, "手机号不存在");
    ErrorCode SMS_SEND_MOBILE_TEMPLATE_PARAM_MISS = new ErrorCode(1_000_024_009, "短信模板参数缺失");
    ErrorCode SMS_SEND_TEMPLATE_NOT_EXISTS = new ErrorCode(1_000_024_010, "短信模板不存在");

    // ========== 邮件错误码 1-000-025-000 ==========
    ErrorCode MAIL_ACCOUNT_NOT_EXISTS = new ErrorCode(1_000_025_000, "邮件账号不存在");
    ErrorCode MAIL_ACCOUNT_RELATE_TEMPLATE_EXISTS = new ErrorCode(1_000_025_001, "邮件账号关联模板存在");
    ErrorCode MAIL_TEMPLATE_CODE_EXISTS = new ErrorCode(1_000_025_002, "邮件模板编码已存在");
    ErrorCode MAIL_SEND_MAIL_NOT_EXISTS = new ErrorCode(1_000_025_003, "邮件不存在");
    ErrorCode MAIL_SEND_TEMPLATE_PARAM_MISS = new ErrorCode(1_000_025_004, "邮件模板参数缺失");

    // ========== 租户错误码 1-000-026-000 ==========
    ErrorCode TENANT_CAN_NOT_UPDATE_SYSTEM = new ErrorCode(1_000_026_000, "不能修改系统租户");
    ErrorCode TENANT_DISABLE = new ErrorCode(1_000_026_001, "租户已禁用");
    ErrorCode TENANT_PACKAGE_DISABLE = new ErrorCode(1_000_026_002, "租户套餐已禁用");
    ErrorCode TENANT_PACKAGE_NOT_EXISTS = new ErrorCode(1_000_026_003, "租户套餐不存在");
    ErrorCode TENANT_PACKAGE_USED = new ErrorCode(1_000_026_004, "租户套餐已被使用");
    ErrorCode TENANT_EXPIRE = new ErrorCode(1_000_026_005, "租户已过期");

    // ========== OAuth2错误码 1-000-027-000 ==========
    ErrorCode OAUTH2_CLIENT_EXISTS = new ErrorCode(1_000_027_000, "OAuth2客户端已存在");
    ErrorCode OAUTH2_CLIENT_NOT_EXISTS = new ErrorCode(1_000_027_001, "OAuth2客户端不存在");
    ErrorCode OAUTH2_CLIENT_DISABLE = new ErrorCode(1_000_027_002, "OAuth2客户端已禁用");
    ErrorCode OAUTH2_CLIENT_AUTHORIZED_GRANT_TYPE_NOT_EXISTS = new ErrorCode(1_000_027_003, "授权类型不存在");
    ErrorCode OAUTH2_CLIENT_CLIENT_SECRET_ERROR = new ErrorCode(1_000_027_004, "客户端密钥错误");
    ErrorCode OAUTH2_CLIENT_REDIRECT_URI_NOT_MATCH = new ErrorCode(1_000_027_005, "重定向URI不匹配");
    ErrorCode OAUTH2_CLIENT_SCOPE_OVER = new ErrorCode(1_000_027_006, "授权范围超出");
    ErrorCode OAUTH2_CODE_EXPIRE = new ErrorCode(1_000_027_007, "授权码已过期");
    ErrorCode OAUTH2_CODE_NOT_EXISTS = new ErrorCode(1_000_027_008, "授权码不存在");
    ErrorCode OAUTH2_GRANT_CLIENT_ID_MISMATCH = new ErrorCode(1_000_027_009, "客户端ID不匹配");
    ErrorCode OAUTH2_GRANT_REDIRECT_URI_MISMATCH = new ErrorCode(1_000_027_010, "重定向URI不匹配");
    ErrorCode OAUTH2_GRANT_STATE_MISMATCH = new ErrorCode(1_000_027_011, "状态不匹配");

    // ========== 通知错误码 1-000-028-000 ==========
    ErrorCode NOTIFY_SEND_TEMPLATE_PARAM_MISS = new ErrorCode(1_000_028_000, "通知模板参数缺失");
    ErrorCode NOTIFY_TEMPLATE_CODE_DUPLICATE = new ErrorCode(1_000_028_001, "通知模板编码重复");
    ErrorCode NOTIFY_TEMPLATE_NOT_EXISTS = new ErrorCode(1_000_028_002, "通知模板不存在");
    ErrorCode NOTICE_NOT_FOUND = new ErrorCode(1_000_028_003, "通知不存在");

    // ========== 错误码错误码 1-000-029-000 ==========
    ErrorCode ERROR_CODE_DUPLICATE = new ErrorCode(1_000_029_000, "错误码重复");
    ErrorCode ERROR_CODE_NOT_EXISTS = new ErrorCode(1_000_029_001, "错误码不存在");

    // ========== 敏感词错误码 1-000-030-000 ==========
    ErrorCode SENSITIVE_WORD_EXISTS = new ErrorCode(1_000_030_000, "敏感词已存在");
    ErrorCode SENSITIVE_WORD_NOT_EXISTS = new ErrorCode(1_000_030_001, "敏感词不存在");

    // ========== 认证错误码 1-000-031-000 ==========
    ErrorCode AUTH_LOGIN_BAD_CREDENTIALS = new ErrorCode(1_000_031_000, "登录失败，账号密码不正确");
    ErrorCode AUTH_LOGIN_CAPTCHA_CODE_ERROR = new ErrorCode(1_000_031_001, "验证码错误");
    ErrorCode AUTH_LOGIN_USER_DISABLED = new ErrorCode(1_000_031_002, "登录失败，账号被禁用");
    ErrorCode AUTH_MOBILE_NOT_EXISTS = new ErrorCode(1_000_031_003, "手机号不存在");
    ErrorCode AUTH_THIRD_LOGIN_NOT_BIND = new ErrorCode(1_000_031_004, "第三方登录未绑定");

    // ========== 社交相关 1-000-018-000 ==========
    ErrorCode SOCIAL_USER_NOT_EXISTS = new ErrorCode(1_000_018_000, "社交用户不存在");
    ErrorCode SOCIAL_USER_BINDING = new ErrorCode(1_000_018_001, "社交用户已绑定");

    // ========== 系统相关 1-000-019-000 ==========
    ErrorCode SYSTEM_ERROR = new ErrorCode(1_000_019_000, "系统错误");
    ErrorCode SYSTEM_BUSY = new ErrorCode(1_000_019_001, "系统繁忙");
    ErrorCode SYSTEM_MAINTENANCE = new ErrorCode(1_000_019_002, "系统维护中");

}
