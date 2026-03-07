package com.sqlapk.medicine.module.patient.enums;

import com.sqlapk.medicine.framework.common.exception.ErrorCode;

/**
 * Pay 错误码 Core 枚举类
 *
 * pay 系统，使用 1-007-000-000 段
 */
public interface ErrorCodeConstants {

    /**
     * ========== APP 模块 1-007-000-000 ==========
     */
    ErrorCode PAY_APP_NOT_FOUND = new ErrorCode(1007000000, "App 不存在");
    ErrorCode PAY_APP_IS_DISABLE = new ErrorCode(1007000002, "App 已经被禁用");
    ErrorCode PAY_APP_EXIST_TRANSACTION_ORDER_CANT_DELETE =  new ErrorCode(1007000003, "支付应用存在交易中的订单,无法删除");

    /**
     * ========== CHANNEL 模块 1-007-001-000 ==========
     */
    ErrorCode PAY_CHANNEL_NOT_FOUND = new ErrorCode(1007001000, "支付渠道的配置不存在");
    ErrorCode PAY_CHANNEL_IS_DISABLE = new ErrorCode(1007001001, "支付渠道已经禁用");
    ErrorCode PAY_CHANNEL_CLIENT_NOT_FOUND = new ErrorCode(1007001002, "支付渠道的客户端不存在");
    ErrorCode CHANNEL_NOT_EXISTS = new ErrorCode(1007001003, "支付渠道不存在");
    ErrorCode CHANNEL_EXIST_SAME_CHANNEL_ERROR = new ErrorCode(1007001005, "已存在相同的渠道");
    ErrorCode CHANNEL_WECHAT_VERSION_2_MCH_KEY_IS_NULL = new ErrorCode(1007001006,"微信渠道v2版本中商户密钥不可为空");
    ErrorCode CHANNEL_WECHAT_VERSION_3_PRIVATE_KEY_IS_NULL = new ErrorCode(1007001007,"微信渠道v3版本apiclient_key.pem不可为空");
    ErrorCode CHANNEL_WECHAT_VERSION_3_CERT_KEY_IS_NULL = new ErrorCode(1007001008,"微信渠道v3版本中apiclient_cert.pem不可为空");
    ErrorCode PAY_CHANNEL_NOTIFY_VERIFY_FAILED = new ErrorCode(1007001009, "渠道通知校验失败");

    // ========== ORDER 模块 1-007-002-000 ==========

    ErrorCode PAY_ORDER_NOT_FOUND = new ErrorCode(1007002000, "支付订单不存在");
    ErrorCode PAY_ORDER_STATUS_IS_NOT_WAITING = new ErrorCode(1007002001, "支付订单不处于待支付");
    ErrorCode PAY_ORDER_STATUS_IS_NOT_SUCCESS = new ErrorCode(1007002002, "支付订单不处于已支付");
    ErrorCode PAY_ORDER_ERROR_USER = new ErrorCode(1007002003, "支付订单用户不正确");

    /**
     * ========== ORDER 模块(拓展单) 1-007-003-000 ==========
     */
    ErrorCode PAY_ORDER_EXTENSION_NOT_FOUND = new ErrorCode(1007003000, "支付交易拓展单不存在");
    ErrorCode PAY_ORDER_EXTENSION_STATUS_IS_NOT_WAITING = new ErrorCode(1007003001, "支付交易拓展单不处于待支付");
    ErrorCode PAY_ORDER_EXTENSION_STATUS_IS_NOT_SUCCESS = new ErrorCode(1007003002, "支付订单不处于已支付");

    // ========== 支付模块(退款) 1-007-006-000 ==========
    ErrorCode PAY_REFUND_AMOUNT_EXCEED = new ErrorCode(1007006000, "退款金额超过订单可退款金额");
    ErrorCode PAY_REFUND_ALL_REFUNDED = new ErrorCode(1007006001, "订单已经全额退款");
    ErrorCode PAY_REFUND_CHN_ORDER_NO_IS_NULL = new ErrorCode(1007006002, "该订单的渠道订单为空");
    ErrorCode PAY_REFUND_SUCCEED = new ErrorCode(1007006003, "已经退款成功");
    ErrorCode PAY_REFUND_NOT_FOUND = new ErrorCode(1007006004, "支付退款单不存在");

    /**
     * ========== 支付商户信息 1-007-004-000 ==========
     */
    ErrorCode PAY_MERCHANT_NOT_EXISTS = new ErrorCode(1007004000, "支付商户信息不存在");
    ErrorCode PAY_MERCHANT_EXIST_APP_CANT_DELETE = new ErrorCode(1007004001, "支付商户存在支付应用,无法删除");

    // ========== 示例订单 1-007-900-000 ==========
    ErrorCode PAY_DEMO_ORDER_NOT_FOUND = new ErrorCode(100790000, "示例订单不存在");
    ErrorCode PAY_DEMO_ORDER_UPDATE_PAID_STATUS_NOT_UNPAID = new ErrorCode(100790001, "示例订单更新支付状态失败，订单不是【未支付】状态");
    ErrorCode PAY_DEMO_ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR = new ErrorCode(100790002, "示例订单更新支付状态失败，支付单编号不匹配");
    ErrorCode PAY_DEMO_ORDER_UPDATE_PAID_FAIL_PAY_ORDER_STATUS_NOT_SUCCESS = new ErrorCode(100790003, "示例订单更新支付状态失败，支付单状态不是【支付成功】状态");
    ErrorCode PAY_DEMO_ORDER_UPDATE_PAID_FAIL_PAY_PRICE_NOT_MATCH = new ErrorCode(100790004, "示例订单更新支付状态失败，支付单金额不匹配");
    ErrorCode PAY_DEMO_ORDER_REFUND_FAIL_NOT_PAID = new ErrorCode(100790005, "发起退款失败，示例订单未支付");
    ErrorCode PAY_DEMO_ORDER_REFUND_FAIL_REFUNDED = new ErrorCode(100790006, "发起退款失败，示例订单已退款");
    ErrorCode PAY_DEMO_ORDER_REFUND_FAIL_REFUND_NOT_FOUND = new ErrorCode(100790007, "发起退款失败，退款订单不存在");
    ErrorCode PAY_DEMO_ORDER_REFUND_FAIL_REFUND_NOT_SUCCESS = new ErrorCode(100790008, "发起退款失败，退款订单未退款成功");
    ErrorCode PAY_DEMO_ORDER_REFUND_FAIL_REFUND_ORDER_ID_ERROR = new ErrorCode(100790008, "发起退款失败，退款单编号不匹配");
    ErrorCode PAY_DEMO_ORDER_REFUND_FAIL_REFUND_PRICE_NOT_MATCH = new ErrorCode(100790004, "发起退款失败，退款单金额不匹配");

    // ========== 患者 TODO 补充编号 ==========
    ErrorCode PATIENT_NOT_EXISTS = new ErrorCode(100900000, "患者不存在");
    ErrorCode PRESCRIPTIONS_NOT_EXISTS = new ErrorCode(100900001, "处方不存在");
    ErrorCode PHARMACY_OUTPATIENT_NOT_EXISTS = new ErrorCode(100900002, "药学门诊不存在");
    ErrorCode PRESCRIPTION_EXAMINATIONS_NOT_EXISTS = new ErrorCode(100900003, "处方检查项目关系不存在");
    ErrorCode PRESCRIPTION_DETAIL_NOT_EXISTS = new ErrorCode(100900004, "患者处方不存在");
    ErrorCode EXAMINATIONS_NOT_EXISTS = new ErrorCode(100900005, "检查项目不存在");
    ErrorCode MEDICINE_CONSULTATION_NOT_EXISTS = new ErrorCode(100900006, "用药咨询不存在");
    ErrorCode EDUCTION_NOT_EXISTS = new ErrorCode(100900007, "用药教育不存在");
    ErrorCode OUTPATIENT_NOT_EXISTS = new ErrorCode(100900008, "门诊药学服务不存在");
    ErrorCode OUTPATIENT_CHECK_NOT_EXISTS = new ErrorCode(100900008, "处方审核不存在");
    ErrorCode OUTPATIENT_MEDICINE_NOT_EXISTS = new ErrorCode(100900009, "门诊药学服务药学门诊记录不存在");
    ErrorCode OUTPATIENT_EXAM_NOT_EXISTS = new ErrorCode(100900010, "门诊药学检查项目明细不存在");
    ErrorCode OUTPATIENT_EDUCATION_NOT_EXISTS = new ErrorCode(100900011, "门诊处方用药教育明细不存在");
    ErrorCode INPATIENT_ROUND_NOT_EXISTS = new ErrorCode(100900012, "住院药学查房记录不存在");
    ErrorCode INPATIENT_EVALUATION_NOT_EXISTS = new ErrorCode(100900013, "住院药学用药评估不存在");
    ErrorCode INPATIENT_EDUCATION_NOT_EXISTS = new ErrorCode(100900014, "住院用药教育不存在");
    ErrorCode INPATIENT_CONSULTATION_NOT_EXISTS = new ErrorCode(100900015, "主要用药咨询不存在");
    ErrorCode INPATIENT_ADVICE_NOT_EXISTS = new ErrorCode(100900016, "住院药学不合理用药不存在");
    ErrorCode INPATIENT_NOT_EXISTS = new ErrorCode(100900017, "住院药学服务不存在");
    ErrorCode HOME_NOT_EXISTS = new ErrorCode(100900018, "居家药学服务不存在");
    ErrorCode HOME_CONSULTATION_NOT_EXISTS = new ErrorCode(100900019, "居家用药咨询不存在");
    ErrorCode HOME_EDUCATION_NOT_EXISTS = new ErrorCode(100900020, "居家用药教育不存在");
    ErrorCode HOME_EVALUATION_NOT_EXISTS = new ErrorCode(100900021, "居家随访评估不存在");
    ErrorCode HOME_MEDICINE_NOT_EXISTS = new ErrorCode(100900022, "用药清单不存在");
    ErrorCode MEDICINE_RECONCILIATION_NOT_EXISTS = new ErrorCode(100900023, "药物重整不存在");
    ErrorCode MEDICINE_RECONCILIATION_DETAIL_NOT_EXISTS = new ErrorCode(100900024, "药物重整明细不存在");
    ErrorCode SURVEYS_NOT_EXISTS = new ErrorCode(100900025, "问卷不存在");
    ErrorCode ANSWERS_NOT_EXISTS = new ErrorCode(100900026, "问卷不存在");
    ErrorCode OPTIONS_NOT_EXISTS = new ErrorCode(100900027, "问题选项不存在");
    ErrorCode QUESTIONS_NOT_EXISTS = new ErrorCode(100900028, "问卷问题不存在");
}
