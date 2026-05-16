package com.medicine.repository.drug;

import com.medicine.domain.drug.DrugCatalog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DrugCatalogMapper {

    /**
     * 按关键词搜索药品目录。
     * 匹配规则（优先级由 SQL 排序体现）：
     *  1. name_cn 前缀匹配（最精确）
     *  2. pinyin_initials 前缀匹配（拼音首字母）
     *  3. name_cn 模糊匹配
     */
    @Select("""
            <script>
            SELECT id, name_cn, name_en, pinyin_initials, category, status,
                   creator, create_time, updater, update_time, deleted, tenant_id
            FROM drug_catalog
            WHERE tenant_id = #{tenantId}
              AND deleted = 0
              AND status = 'ACTIVE'
            <if test="q != null and q != ''">
              AND (
                    name_cn        LIKE CONCAT(#{q}, '%')
                 OR pinyin_initials LIKE CONCAT(#{q}, '%')
                 OR name_cn        LIKE CONCAT('%', #{q}, '%')
              )
            </if>
            ORDER BY
              CASE
                WHEN name_cn        LIKE CONCAT(#{q}, '%')        THEN 0
                WHEN pinyin_initials LIKE CONCAT(#{q}, '%')        THEN 1
                ELSE 2
              END,
              name_cn ASC
            LIMIT #{limit}
            </script>
            """)
    List<DrugCatalog> search(
            @Param("tenantId") Long tenantId,
            @Param("q") String q,
            @Param("limit") int limit
    );

    @Select("""
            SELECT COUNT(1)
            FROM drug_catalog
            WHERE tenant_id = #{tenantId}
              AND deleted = 0
              AND status = 'ACTIVE'
              AND name_cn = #{nameCn}
            """)
    int countByNameCn(@Param("tenantId") Long tenantId, @Param("nameCn") String nameCn);
}
