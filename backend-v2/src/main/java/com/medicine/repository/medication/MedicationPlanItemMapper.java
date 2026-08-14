package com.medicine.repository.medication;

import com.medicine.domain.medication.MedicationPlanItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MedicationPlanItemMapper {

    @Insert("""
            INSERT INTO medication_plan_item (
                plan_id, drug_name, drug_spec, dosage, frequency_code, time_points_json, meal_relation, route, instructions, sort_order,
                creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{planId}, #{drugName}, #{drugSpec}, #{dosage}, #{frequencyCode}, #{timePointsJson}, #{mealRelation}, #{route}, #{instructions}, #{sortOrder},
                #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MedicationPlanItem item);

    @Select("""
            SELECT id, plan_id, drug_name, drug_spec, dosage, frequency_code, time_points_json, meal_relation, route, instructions, sort_order,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM medication_plan_item
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    MedicationPlanItem selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            SELECT id, plan_id, drug_name, drug_spec, dosage, frequency_code, time_points_json, meal_relation, route, instructions, sort_order,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM medication_plan_item
            WHERE tenant_id = #{tenantId} AND plan_id = #{planId} AND deleted = 0
            ORDER BY sort_order ASC, id ASC
            """)
    List<MedicationPlanItem> selectByPlanId(@Param("tenantId") Long tenantId, @Param("planId") Long planId);

    @Update("""
            UPDATE medication_plan_item
            SET deleted = b'1',
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND plan_id = #{planId} AND deleted = 0
            """)
    int softDeleteByPlanId(
            @Param("tenantId") Long tenantId,
            @Param("planId") Long planId,
            @Param("updater") String updater,
            @Param("updateTime") LocalDateTime updateTime
    );
}
