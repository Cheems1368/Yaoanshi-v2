package com.medicine.repository.medication;

import com.medicine.domain.medication.MedicationPlanEvent;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MedicationPlanEventMapper {

    @Insert("""
            INSERT INTO medication_plan_event (
                plan_id, event_type, content, operator_type, operator_id,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{planId}, #{eventType}, #{content}, #{operatorType}, #{operatorId},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MedicationPlanEvent event);

    @Select("""
            SELECT id, plan_id, event_type, content, operator_type, operator_id,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM medication_plan_event
            WHERE tenant_id = #{tenantId} AND plan_id = #{planId} AND deleted = 0
            ORDER BY create_time ASC, id ASC
            """)
    List<MedicationPlanEvent> selectByPlanId(@Param("tenantId") Long tenantId, @Param("planId") Long planId);
}
