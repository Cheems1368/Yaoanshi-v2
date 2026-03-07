package com.sqlapk.medicine.module.patient.dal.mysql.patient;

import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.module.patient.dal.dataobject.patient.PatientAuditRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 患者档案审核记录 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface PatientAuditRecordMapper extends BaseMapperX<PatientAuditRecordDO> {

    /**
     * 根据患者ID获取最新的审核记录
     */
    default PatientAuditRecordDO selectLatestByPatientId(Long patientId) {
        return selectOne("patient_id", patientId, "create_time", false);
    }

    /**
     * 根据患者ID获取审核历史
     */
    default List<PatientAuditRecordDO> selectListByPatientId(Long patientId) {
        return selectList(PatientAuditRecordDO::getPatientId, patientId);
    }

    /**
     * 根据审核状态获取审核记录列表
     */
    default List<PatientAuditRecordDO> selectListByAuditStatus(Integer auditStatus) {
        return selectList(PatientAuditRecordDO::getAuditStatus, auditStatus);
    }

    /**
     * 根据审核人ID获取审核记录列表
     */
    default List<PatientAuditRecordDO> selectListByAuditorId(Long auditorId) {
        return selectList(PatientAuditRecordDO::getAuditorId, auditorId);
    }

    /**
     * 统计待审核记录数量
     */
    default Long countPendingAudits() {
        return selectCount("audit_status", 0);
    }

}
