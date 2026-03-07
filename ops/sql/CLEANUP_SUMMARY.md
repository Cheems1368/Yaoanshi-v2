# SQL 目录清理总结

## 清理时间
2025-12-23

## 清理内容

### 1. 删除重复文件
- ✅ 删除 `help_orders.sql`（保留 `help_orders_fixed.sql` 并重命名为 `help_orders.sql`）
- ✅ 删除 `help_order_permissions.sql`（保留 `help_order_permissions_fixed.sql` 并重命名为 `help_order_permissions.sql`）
- ✅ 删除 `patient_profile_enhancement.sql`（保留更完整的 `patient-profile-enhancement.sql`）

### 2. 合并性能优化文件
- ✅ 删除 `performance-indexes-basic.sql`（内容已包含在 `performance-indexes.sql` 中）
- ✅ 删除 `performance-indexes-composite.sql`（内容已包含在 `performance-indexes.sql` 中）
- ✅ 保留 `performance-indexes.sql`（最完整的索引优化脚本，189行）
- ✅ 保留 `performance-optimization.sql`（数据库性能参数优化脚本）

### 3. 归档大型文件
- ✅ 将 `medicine.sql` (15MB) 移动到 `archive/` 目录
- ✅ 保留 `new_medicine.sql` (8KB) 作为日常使用的精简版本

## 当前文件结构

### 核心 SQL 文件
- `help_orders.sql` - 工单表结构
- `help_order_permissions.sql` - 工单权限配置
- `patient-profile-enhancement.sql` - 患者档案增强（367行，完整版本）
- `performance-indexes.sql` - 性能索引优化（189行，包含基础和复合索引）
- `performance-optimization.sql` - 数据库性能参数优化
- `new_medicine.sql` - 药品表结构（精简版）

### 功能模块 SQL
- `bind_roles.sql` - 角色绑定
- `blood_pressure_records.sql` - 血压记录
- `chat_messages.sql` - 聊天消息
- `create_test_data.sql` - 测试数据
- `electronic_prescriptions.sql` - 电子处方
- `init-reminder-tables.sql` - 提醒表初始化
- `medication_mock_data.sql` - 用药模拟数据
- `security-hardening.sql` - 安全加固
- `simple-init.sql` - 简单初始化
- `user_management.sql` - 用户管理

### 数据库迁移
- `migrations/` - Flyway/Liquibase 迁移脚本
  - `V2025.09.21.001__fix_user_encoding_and_patient_permissions.sql`
  - `V2025.12.08.001__create_patients_table.sql`

### MySQL 初始化
- `mysql/ruoyi-vue-pro.sql` - 主数据库初始化脚本
- `mysql/optional/` - 可选组件初始化脚本

### 归档文件
- `archive/medicine.sql.backup` - 完整数据库备份（15MB）

## 清理效果

- 删除了 5 个冗余文件
- 归档了 1 个大文件（15MB）
- 统一了文件命名规范（去除了 `_fixed` 后缀）
- 保留了所有必要的功能文件

## 注意事项

1. **大型备份文件**: `archive/medicine.sql.backup` 仅在需要完整数据恢复时使用
2. **性能优化**: `performance-indexes.sql` 包含了所有索引优化，无需再运行基础或复合索引脚本
3. **患者档案**: 使用 `patient-profile-enhancement.sql` 作为患者档案增强的标准脚本
4. **工单系统**: `help_orders.sql` 和 `help_order_permissions.sql` 已更新为最新版本

## 后续建议

1. 定期清理临时 SQL 脚本
2. 将大型备份文件移至外部存储或版本控制系统
3. 使用数据库迁移工具（Flyway/Liquibase）管理版本化的 SQL 脚本
4. 为每个 SQL 文件添加清晰的注释说明用途和版本

