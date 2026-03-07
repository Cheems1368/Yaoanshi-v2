-- =============================================
-- 创建 patients 表
-- 版本: V2025.12.08.001
-- 描述: 创建患者表，确保数据库重启后表结构不丢失
-- =============================================

-- 如果表不存在则创建
CREATE TABLE IF NOT EXISTS `patients` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '患者ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `patient_type` smallint NOT NULL DEFAULT '0' COMMENT '患者类型（住院(0)、门诊(1)、居家(2)）',
  `insurance` smallint NOT NULL DEFAULT '0' COMMENT '报销类型（医保(0)、非医保(1)）',
  `address` varchar(255) NOT NULL COMMENT '家庭住址',
  `insurance_card_number` varchar(255) DEFAULT NULL COMMENT '医保卡号',
  `allergy_history` text COMMENT '过敏史',
  `medical_history` text COMMENT '患病史',
  `family_history` text COMMENT '家族史',
  `personal_history` text COMMENT '个人史',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  `birthday` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_patient_type` (`patient_type`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者表';

