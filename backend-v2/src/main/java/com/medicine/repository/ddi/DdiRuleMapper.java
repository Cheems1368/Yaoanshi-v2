package com.medicine.repository.ddi;

import com.medicine.domain.ddi.DdiRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DdiRuleMapper {

    @Select("""
            SELECT id, rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, maintained_by_pharmacist_id,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM ddi_rule
            WHERE tenant_id = #{tenantId} AND deleted = 0 AND status = 'ACTIVE'
            ORDER BY id ASC
            """)
    List<DdiRule> selectActiveRules(@Param("tenantId") Long tenantId);

    @Select("""
            SELECT id, rule_code, drug_a_name, drug_b_name, severity, message, recommended_action, status, maintained_by_pharmacist_id,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM ddi_rule
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            LIMIT 1
            """)
    DdiRule selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);
}
