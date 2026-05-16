package com.medicine.repository.education;

import com.medicine.domain.education.EducationContentCard;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EducationContentCardMapper {

    @Select("""
            SELECT id, content_code, title, summary, body, category, category_name, diagnosis_tags, drug_tags, population_tags,
                   source, review_status, reviewed_by, reviewed_at, creator, create_time, updater, update_time, deleted, tenant_id
            FROM education_content_card
            WHERE tenant_id = #{tenantId} AND content_code = #{contentCode} AND deleted = 0
            LIMIT 1
            """)
    EducationContentCard selectByContentCode(@Param("tenantId") Long tenantId, @Param("contentCode") String contentCode);

    @Select("""
            SELECT id, content_code, title, summary, body, category, category_name, diagnosis_tags, drug_tags, population_tags,
                   source, review_status, reviewed_by, reviewed_at, creator, create_time, updater, update_time, deleted, tenant_id
            FROM education_content_card
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            LIMIT 1
            """)
    EducationContentCard selectById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            <script>
            SELECT id, content_code, title, summary, body, category, category_name, diagnosis_tags, drug_tags, population_tags,
                   source, review_status, reviewed_by, reviewed_at, creator, create_time, updater, update_time, deleted, tenant_id
            FROM education_content_card
            WHERE tenant_id = #{tenantId} AND deleted = 0
            <if test="reviewStatus != null and reviewStatus != ''">
              AND review_status = #{reviewStatus}
            </if>
            <if test="category != null and category != ''">
              AND category = #{category}
            </if>
            <if test="keyword != null and keyword != ''">
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR summary LIKE CONCAT('%', #{keyword}, '%') OR body LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            ORDER BY update_time DESC, id DESC
            LIMIT #{offset}, #{pageSize}
            </script>
            """)
    List<EducationContentCard> search(
            @Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("reviewStatus") String reviewStatus,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM education_content_card
            WHERE tenant_id = #{tenantId} AND deleted = 0
            <if test="reviewStatus != null and reviewStatus != ''">
              AND review_status = #{reviewStatus}
            </if>
            <if test="category != null and category != ''">
              AND category = #{category}
            </if>
            <if test="keyword != null and keyword != ''">
              AND (title LIKE CONCAT('%', #{keyword}, '%') OR summary LIKE CONCAT('%', #{keyword}, '%') OR body LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    long countSearch(
            @Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("reviewStatus") String reviewStatus
    );

    @Select("""
            <script>
            SELECT id, content_code, title, summary, body, category, category_name, diagnosis_tags, drug_tags, population_tags,
                   source, review_status, reviewed_by, reviewed_at, creator, create_time, updater, update_time, deleted, tenant_id
            FROM education_content_card
            WHERE tenant_id = #{tenantId} AND deleted = 0 AND review_status = 'REVIEWED'
              AND (
                diagnosis_tags LIKE '%GENERAL%'
                <foreach collection="tags" item="tag">
                  OR diagnosis_tags LIKE CONCAT('%', #{tag}, '%')
                  OR drug_tags LIKE CONCAT('%', #{tag}, '%')
                  OR population_tags LIKE CONCAT('%', #{tag}, '%')
                </foreach>
              )
            ORDER BY
              CASE WHEN diagnosis_tags LIKE '%GENERAL%' THEN 1 ELSE 0 END,
              update_time DESC,
              id DESC
            LIMIT #{limit}
            </script>
            """)
    List<EducationContentCard> selectReviewedRecommendations(
            @Param("tenantId") Long tenantId,
            @Param("tags") List<String> tags,
            @Param("limit") int limit
    );

    @Insert("""
            INSERT INTO education_content_card (
                content_code, title, summary, body, category, category_name, diagnosis_tags, drug_tags, population_tags,
                source, review_status, reviewed_by, reviewed_at, creator, create_time, updater, update_time, deleted, tenant_id
            ) VALUES (
                #{contentCode}, #{title}, #{summary}, #{body}, #{category}, #{categoryName}, #{diagnosisTags}, #{drugTags}, #{populationTags},
                #{source}, #{reviewStatus}, #{reviewedBy}, #{reviewedAt}, #{creator}, #{createTime}, #{updater}, #{updateTime}, #{deleted}, #{tenantId}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(EducationContentCard card);

    @Update("""
            UPDATE education_content_card
            SET title = #{title},
                summary = #{summary},
                body = #{body},
                category = #{category},
                category_name = #{categoryName},
                diagnosis_tags = #{diagnosisTags},
                drug_tags = #{drugTags},
                population_tags = #{populationTags},
                source = #{source},
                review_status = #{reviewStatus},
                reviewed_by = #{reviewedBy},
                reviewed_at = #{reviewedAt},
                updater = #{updater},
                update_time = #{updateTime}
            WHERE tenant_id = #{tenantId} AND id = #{id} AND deleted = 0
            """)
    int update(EducationContentCard card);
}
