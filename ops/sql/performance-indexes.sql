-- ===========================================
-- 性能优化索引脚本
-- ===========================================
-- 用于建立p95≤300ms的性能基线

-- ===========================================
-- 工单表性能索引
-- ===========================================

-- 工单状态和租户索引
CREATE INDEX IF NOT EXISTS idx_workorder_status_tenant 
ON work_order(status, tenant_id);

-- 工单创建时间索引
CREATE INDEX IF NOT EXISTS idx_workorder_created_time 
ON work_order(create_time);

-- 工单分配人索引
CREATE INDEX IF NOT EXISTS idx_workorder_assignee 
ON work_order(assignee_id);

-- 工单患者索引
CREATE INDEX IF NOT EXISTS idx_workorder_patient 
ON work_order(patient_id);

-- 工单类型索引
CREATE INDEX IF NOT EXISTS idx_workorder_type 
ON work_order(type);

-- ===========================================
-- 患者表性能索引
-- ===========================================

-- 患者租户索引
CREATE INDEX IF NOT EXISTS idx_patient_tenant 
ON patient(tenant_id);

-- 患者创建时间索引
CREATE INDEX IF NOT EXISTS idx_patient_created_time 
ON patient(create_time);

-- 患者姓名索引
CREATE INDEX IF NOT EXISTS idx_patient_name 
ON patient(name);

-- 患者手机号索引
CREATE INDEX IF NOT EXISTS idx_patient_mobile 
ON patient(mobile);

-- ===========================================
-- 用药计划表性能索引
-- ===========================================

-- 用药计划患者索引
CREATE INDEX IF NOT EXISTS idx_medication_plan_patient 
ON medication_plan(patient_id);

-- 用药计划状态索引
CREATE INDEX IF NOT EXISTS idx_medication_plan_status 
ON medication_plan(status);

-- 用药计划创建时间索引
CREATE INDEX IF NOT EXISTS idx_medication_plan_created_time 
ON medication_plan(create_time);

-- 用药计划开始时间索引
CREATE INDEX IF NOT EXISTS idx_medication_plan_start_time 
ON medication_plan(start_time);

-- ===========================================
-- 用药打卡表性能索引
-- ===========================================

-- 用药打卡计划索引
CREATE INDEX IF NOT EXISTS idx_medication_checkin_plan 
ON medication_checkin(plan_id);

-- 用药打卡时间索引
CREATE INDEX IF NOT EXISTS idx_medication_checkin_time 
ON medication_checkin(checkin_time);

-- 用药打卡状态索引
CREATE INDEX IF NOT EXISTS idx_medication_checkin_status 
ON medication_checkin(status);

-- ===========================================
-- 用户表性能索引
-- ===========================================

-- 用户租户索引
CREATE INDEX IF NOT EXISTS idx_system_users_tenant 
ON system_users(tenant_id);

-- 用户状态索引
CREATE INDEX IF NOT EXISTS idx_system_users_status 
ON system_users(status);

-- 用户创建时间索引
CREATE INDEX IF NOT EXISTS idx_system_users_created_time 
ON system_users(create_time);

-- ===========================================
-- 部门表性能索引
-- ===========================================

-- 部门租户索引
CREATE INDEX IF NOT EXISTS idx_system_dept_tenant 
ON system_dept(tenant_id);

-- 部门状态索引
CREATE INDEX IF NOT EXISTS idx_system_dept_status 
ON system_dept(status);

-- 部门父级索引
CREATE INDEX IF NOT EXISTS idx_system_dept_parent 
ON system_dept(parent_id);

-- ===========================================
-- 角色表性能索引
-- ===========================================

-- 角色租户索引
CREATE INDEX IF NOT EXISTS idx_system_role_tenant 
ON system_role(tenant_id);

-- 角色状态索引
CREATE INDEX IF NOT EXISTS idx_system_role_status 
ON system_role(status);

-- ===========================================
-- 权限表性能索引
-- ===========================================

-- 权限类型索引
CREATE INDEX IF NOT EXISTS idx_system_permission_type 
ON system_permission(type);

-- 权限状态索引
CREATE INDEX IF NOT EXISTS idx_system_permission_status 
ON system_permission(status);

-- ===========================================
-- 操作日志表性能索引
-- ===========================================

-- 操作日志用户索引
CREATE INDEX IF NOT EXISTS idx_system_operate_log_user 
ON system_operate_log(user_id);

-- 操作日志时间索引
CREATE INDEX IF NOT EXISTS idx_system_operate_log_time 
ON system_operate_log(start_time);

-- 操作日志模块索引
CREATE INDEX IF NOT EXISTS idx_system_operate_log_module 
ON system_operate_log(module);

-- ===========================================
-- 登录日志表性能索引
-- ===========================================

-- 登录日志用户索引
CREATE INDEX IF NOT EXISTS idx_system_login_log_user 
ON system_login_log(user_id);

-- 登录日志时间索引
CREATE INDEX IF NOT EXISTS idx_system_login_log_time 
ON system_login_log(login_time);

-- 登录日志IP索引
CREATE INDEX IF NOT EXISTS idx_system_login_log_ip 
ON system_login_log(user_ip);

-- ===========================================
-- 性能优化完成提示
-- ===========================================
-- 性能优化索引创建完成！
-- 请验证以下内容：
-- 1. 所有索引创建成功
-- 2. 查询性能提升
-- 3. 响应时间达到p95≤300ms
-- 4. 数据库连接池配置优化
-- 
-- 重要提醒：
-- - 定期监控索引使用情况
-- - 根据实际查询模式调整索引
-- - 定期分析慢查询日志
-- - 监控数据库性能指标
