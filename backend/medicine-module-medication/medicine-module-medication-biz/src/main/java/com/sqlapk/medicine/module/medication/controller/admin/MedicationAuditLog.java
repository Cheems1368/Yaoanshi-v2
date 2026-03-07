package com.sqlapk.medicine.module.medication.controller.admin;

import com.sqlapk.medicine.framework.operatelog.core.enums.OperateTypeEnum;

/**
 * 用药打卡系统审计日志注解
 * 
 * 用于标记需要审计的关键操作
 *
 * @author sqlapk
 */
public class MedicationAuditLog {

    /**
     * 用药计划创建审计日志
     */
    public static class CreateMedPlan {
        public static final String MODULE = "用药打卡系统";
        public static final String NAME = "创建用药计划";
        public static final OperateTypeEnum TYPE = OperateTypeEnum.CREATE;
        public static final String CONTENT = "为患者创建用药计划";
    }

    /**
     * 用药计划修改审计日志
     */
    public static class UpdateMedPlan {
        public static final String MODULE = "用药打卡系统";
        public static final String NAME = "修改用药计划";
        public static final OperateTypeEnum TYPE = OperateTypeEnum.UPDATE;
        public static final String CONTENT = "修改患者用药计划";
    }

    /**
     * 用药计划删除审计日志
     */
    public static class DeleteMedPlan {
        public static final String MODULE = "用药打卡系统";
        public static final String NAME = "删除用药计划";
        public static final OperateTypeEnum TYPE = OperateTypeEnum.DELETE;
        public static final String CONTENT = "删除患者用药计划";
    }

    /**
     * 用药计划状态变更审计日志
     */
    public static class UpdateMedPlanStatus {
        public static final String MODULE = "用药打卡系统";
        public static final String NAME = "用药计划状态变更";
        public static final OperateTypeEnum TYPE = OperateTypeEnum.UPDATE;
        public static final String CONTENT = "变更用药计划状态";
    }

    /**
     * 用药打卡记录创建审计日志
     */
    public static class CreateCheckin {
        public static final String MODULE = "用药打卡系统";
        public static final String NAME = "创建用药打卡记录";
        public static final OperateTypeEnum TYPE = OperateTypeEnum.CREATE;
        public static final String CONTENT = "患者进行用药打卡";
    }

    /**
     * 用药打卡记录修改审计日志
     */
    public static class UpdateCheckin {
        public static final String MODULE = "用药打卡系统";
        public static final String NAME = "修改用药打卡记录";
        public static final OperateTypeEnum TYPE = OperateTypeEnum.UPDATE;
        public static final String CONTENT = "修改用药打卡记录";
    }

    /**
     * 用药打卡记录删除审计日志
     */
    public static class DeleteCheckin {
        public static final String MODULE = "用药打卡系统";
        public static final String NAME = "删除用药打卡记录";
        public static final OperateTypeEnum TYPE = OperateTypeEnum.DELETE;
        public static final String CONTENT = "删除用药打卡记录";
    }

    /**
     * 用药通知发送审计日志
     */
    public static class SendNotification {
        public static final String MODULE = "用药打卡系统";
        public static final String NAME = "发送用药通知";
        public static final OperateTypeEnum TYPE = OperateTypeEnum.CREATE;
        public static final String CONTENT = "发送用药提醒通知";
    }

    /**
     * 用药报告生成审计日志
     */
    public static class GenerateReport {
        public static final String MODULE = "用药打卡系统";
        public static final String NAME = "生成用药报告";
        public static final OperateTypeEnum TYPE = OperateTypeEnum.CREATE;
        public static final String CONTENT = "生成患者用药报告";
    }

    /**
     * 用药报告下载审计日志
     */
    public static class DownloadReport {
        public static final String MODULE = "用药打卡系统";
        public static final String NAME = "下载用药报告";
        public static final OperateTypeEnum TYPE = OperateTypeEnum.GET;
        public static final String CONTENT = "下载患者用药报告";
    }

    /**
     * 用药依从性统计审计日志
     */
    public static class CalculateAdherence {
        public static final String MODULE = "用药打卡系统";
        public static final String NAME = "计算用药依从性";
        public static final OperateTypeEnum TYPE = OperateTypeEnum.GET;
        public static final String CONTENT = "计算患者用药依从性统计";
    }
}